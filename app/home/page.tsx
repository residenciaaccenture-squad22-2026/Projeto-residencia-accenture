"use client";

import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState, useSyncExternalStore } from "react";
import { formatSupabaseError, normalizarTexto } from "@/lib/app-utils";
import {
  clearFuncionarioSession,
  getFuncionarioServerSnapshot,
  getFuncionarioSnapshot,
  isAdministrador,
  parseFuncionario,
  subscribeFuncionarioSnapshot,
} from "@/lib/session";
import { isSupabaseConfigured, supabase } from "@/lib/supabase";
import styles from "./home.module.css";

const STATUS_RESERVA_ATIVA = "ATIVA";
const STATUS_RESERVA_CANCELADA = "CANCELADA";

type Sala = {
  id: number | string;
  nome?: string | null;
  localizacao?: string | null;
  capacidade?: number | string | null;
  status?: string | null;
  disponivel?: boolean | null;
  motivo_manutencao?: string | null;
};

type Posicao = {
  id: number | string;
  sala_id?: number | string | null;
  codigo_cadeira?: string | null;
  status?: string | null;
  disponivel?: boolean | null;
  motivo_manutencao?: string | null;
};

type Mesa = {
  id: number | string;
  sala_id?: number | string | null;
};

type Equipamento = {
  id: number | string;
  posicao_id?: number | string | null;
  nome?: string | null;
  tipo?: string | null;
  descricao?: string | null;
  status?: string | null;
};

function getTodayInputValue() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

function montarDataHora(data: string, hora: string) {
  return data && hora ? `${data}T${hora}:00` : "";
}

function isSalaDisponivel(sala: Sala) {
  const status = normalizarTexto(sala.status);
  if (status === "manutencao" || status === "indisponivel") {
    return false;
  }

  return sala.disponivel !== false;
}

function isPosicaoDisponivel(posicao: Posicao, sala?: Sala | null) {
  const status = normalizarTexto(posicao.status);
  if (status === "manutencao" || status === "indisponivel") {
    return false;
  }

  return posicao.disponivel !== false && (!sala || isSalaDisponivel(sala));
}

function motivoIndisponibilidadeSala(sala: Sala) {
  if (isSalaDisponivel(sala)) {
    return "";
  }

  return sala.motivo_manutencao || "Ambiente indisponível para reserva.";
}

function motivoIndisponibilidadePosicao(posicao: Posicao, sala?: Sala | null) {
  if (sala && !isSalaDisponivel(sala)) {
    return motivoIndisponibilidadeSala(sala);
  }

  if (isPosicaoDisponivel(posicao, sala)) {
    return "";
  }

  return posicao.motivo_manutencao || "Posição indisponível para reserva.";
}

function podeTerMultiplasReservas(funcionario: ReturnType<typeof parseFuncionario>) {
  const role = normalizarTexto(funcionario?.role);
  const cargo = normalizarTexto(funcionario?.cargo);

  return ["gestor", "gerente", "admin", "administrador"].includes(role)
    || ["gestor", "gerente", "admin", "administrador"].includes(cargo);
}

function statusCanceladoFilter() {
  return `(${STATUS_RESERVA_CANCELADA},Cancelada,cancelada)`;
}

export default function Home() {
  const router = useRouter();
  const funcionarioSnapshot = useSyncExternalStore(
    subscribeFuncionarioSnapshot,
    getFuncionarioSnapshot,
    getFuncionarioServerSnapshot
  );
  const funcionario = useMemo(() => parseFuncionario(funcionarioSnapshot), [funcionarioSnapshot]);
  const [salas, setSalas] = useState<Sala[]>([]);
  const [mesas, setMesas] = useState<Mesa[]>([]);
  const [busca, setBusca] = useState("");
  const [salaSelecionada, setSalaSelecionada] = useState<Sala | null>(null);
  const [posicoes, setPosicoes] = useState<Posicao[]>([]);
  const [posicoesSemSala, setPosicoesSemSala] = useState<Posicao[]>([]);
  const [posicoesAviso, setPosicoesAviso] = useState("");
  const [equipamentosPorPosicao, setEquipamentosPorPosicao] = useState<Record<string, Equipamento[]>>({});
  const [dataReserva, setDataReserva] = useState(getTodayInputValue);
  const [horaInicio, setHoraInicio] = useState("09:00");
  const [horaFim, setHoraFim] = useState("18:00");
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingPosicoes, setIsLoadingPosicoes] = useState(false);
  const [isReservando, setIsReservando] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"error" | "success">("error");

  useEffect(() => {
    buscarSalas();
  }, []);

  useEffect(() => {
    if (salaSelecionada) {
      buscarPosicoesDaSala(salaSelecionada);
    }
  }, [salaSelecionada]);

  async function buscarSalas() {
    setIsLoading(true);
    setMessage("");

    if (!isSupabaseConfigured) {
      setIsLoading(false);
      setMessageType("error");
      setMessage("Supabase não configurado. Verifique NEXT_PUBLIC_SUPABASE_URL e NEXT_PUBLIC_SUPABASE_ANON_KEY no .env.local.");
      return;
    }

    const [salasResult, mesasResult] = await Promise.all([
      supabase.from("sala").select("*").order("nome", { ascending: true }),
      supabase.from("mesa").select("id, sala_id").order("id", { ascending: true }),
    ]);

    setIsLoading(false);

    if (salasResult.error) {
      console.error("Erro ao carregar salas.", salasResult.error);
      setMessageType("error");
      setMessage(formatSupabaseError("Não foi possível carregar as salas", salasResult.error));
      return;
    }

    if (mesasResult.error) {
      console.warn("Tabela mesa indisponível ou sem permissão. A tela continuará sem contagem de mesas.", mesasResult.error);
    }

    console.info("Supabase conectado. Salas carregadas:", salasResult.data?.length || 0);
    setSalas(salasResult.data || []);
    setMesas((mesasResult.data || []) as unknown as Mesa[]);
  }

  async function buscarPosicoesDaSala(sala: Sala) {
    const usarBuscaCorrigida = true;

    if (usarBuscaCorrigida) {
      setIsLoadingPosicoes(true);
      setMessage("");
      setPosicoesAviso("");
      setPosicoesSemSala([]);
      setEquipamentosPorPosicao({});
      console.log("Sala selecionada:", sala);
      console.log("Buscando posições da sala:", sala?.id);

      const posicoesDaSala = await supabase
        .from("posicao")
        .select("*")
        .eq("sala_id", sala.id)
        .order("id", { ascending: true });

      console.log("Posições retornadas:", posicoesDaSala.data);

      if (posicoesDaSala.error) {
        console.error("Erro ao buscar posições:", posicoesDaSala.error);
        console.error("Erro ao buscar posições da sala.", posicoesDaSala.error);
        setIsLoadingPosicoes(false);
        setMessageType("error");
        setMessage(formatSupabaseError("Não foi possível carregar as posições desta sala", posicoesDaSala.error));
        setPosicoes([]);
        setPosicoesSemSala([]);
        return;
      }

      const posicoesData = (posicoesDaSala.data || []) as unknown as Posicao[];
      let posicoesSemSalaData: Posicao[] = [];

      if (posicoesData.length === 0) {
        const todasPosicoes = await supabase.from("posicao").select("*").order("id", { ascending: true });

        if (todasPosicoes.error) {
          console.error("Erro ao verificar posições cadastradas.", todasPosicoes.error);
          setMessageType("error");
          setMessage(formatSupabaseError("Não foi possível verificar as posições cadastradas", todasPosicoes.error));
      } else if ((todasPosicoes.data || []).length === 0) {
        setPosicoesAviso("Nenhuma posição encontrada para esta sala.");
        setMessageType("error");
        setMessage("Nenhuma posição encontrada para esta sala.");
      } else {
        setPosicoesAviso("As posições existem, mas precisam estar vinculadas a uma sala pelo campo sala_id.");
        setMessageType("error");
        setMessage("As posições existem, mas precisam estar vinculadas a uma sala pelo campo sala_id.");
      }
      }

      const posicoesSemSalaResult = await supabase
        .from("posicao")
        .select("*")
        .is("sala_id", null)
        .order("id", { ascending: true });

      if (posicoesSemSalaResult.error) {
        console.error("Erro ao buscar posições sem sala vinculada.", posicoesSemSalaResult.error);
      } else {
        posicoesSemSalaData = (posicoesSemSalaResult.data || []) as unknown as Posicao[];
        if (posicoesData.length === 0 && posicoesSemSalaData.length > 0) {
          console.warn("Existem posições cadastradas, mas elas não estão vinculadas a esta sala.", {
            salaId: sala.id,
            semSala: posicoesSemSalaData.length,
          });
        }
      }

      console.info("Posições carregadas para a sala:", {
        salaId: sala.id,
        vinculadas: posicoesData.length,
        semSala: posicoesSemSalaData.length,
      });
      setPosicoes(posicoesData);
      setPosicoesSemSala(posicoesSemSalaData);

      const posicaoIds = [...posicoesData, ...posicoesSemSalaData].map((posicao) => posicao.id).filter(Boolean);
      if (posicaoIds.length > 0) {
        const equipamentosResult = await supabase
          .from("equipamento")
          .select("id, posicao_id, nome, tipo, descricao, status")
          .in("posicao_id", posicaoIds);

        if (!equipamentosResult.error) {
          const agrupados = ((equipamentosResult.data || []) as unknown as Equipamento[]).reduce<
            Record<string, Equipamento[]>
          >((acc, equipamento) => {
            if (equipamento.posicao_id) {
              const key = String(equipamento.posicao_id);
              acc[key] = [...(acc[key] || []), equipamento];
            }

            return acc;
          }, {});

          setEquipamentosPorPosicao(agrupados);
        } else {
          console.warn("Equipamentos não carregados. A tela seguirá sem equipamentos.", equipamentosResult.error);
        }
      }

      setIsLoadingPosicoes(false);
      return;
    }

  }

  async function validarReservaUsuarioComum(inicioReserva: string) {
    if (!funcionario || podeTerMultiplasReservas(funcionario)) {
      return true;
    }

    const { data, error } = await supabase
      .from("reserva")
      .select("id")
      .eq("funcionario_id", funcionario.id)
      .not("status", "in", statusCanceladoFilter())
      .gt("data_fim", inicioReserva)
      .limit(1);

    if (error) {
      throw new Error(formatSupabaseError("Não foi possível validar suas reservas ativas", error));
    }

    return (data || []).length === 0;
  }

  async function handleReservarPosicao(posicao: Posicao) {
    if (!funcionario) {
      setMessageType("error");
      setMessage("Faça login para reservar uma posição.");
      router.push("/");
      return;
    }

    if (salaSelecionada && !isSalaDisponivel(salaSelecionada)) {
      setMessageType("error");
      setMessage(motivoIndisponibilidadeSala(salaSelecionada));
      return;
    }

    if (!isPosicaoDisponivel(posicao, salaSelecionada)) {
      setMessageType("error");
      setMessage(motivoIndisponibilidadePosicao(posicao, salaSelecionada));
      return;
    }

    const inicioReserva = montarDataHora(dataReserva, horaInicio);
    const fimReserva = montarDataHora(dataReserva, horaFim);
    const inicioDate = new Date(inicioReserva);
    const fimDate = new Date(fimReserva);

    if (!inicioReserva || !fimReserva || Number.isNaN(inicioDate.getTime()) || Number.isNaN(fimDate.getTime())) {
      setMessageType("error");
      setMessage("Informe data, horário de início e horário de fim.");
      return;
    }

    if (inicioDate >= fimDate) {
      setMessageType("error");
      setMessage("O horário de início deve ser anterior ao horário de fim.");
      return;
    }

    setIsReservando(true);
    setMessage("");

    try {
      const usuarioPodeReservar = await validarReservaUsuarioComum(inicioReserva);
      if (!usuarioPodeReservar) {
        setMessageType("error");
        setMessage("Usuário comum pode manter apenas uma reserva ativa. Cancele a reserva atual antes de criar outra.");
        return;
      }

      const { data: reservasConflitantes, error: reservaAtivaError } = await supabase
        .from("reserva")
        .select("id")
        .eq("posicao_id", posicao.id)
        .not("status", "in", statusCanceladoFilter())
        .lt("data_inicio", fimReserva)
        .gt("data_fim", inicioReserva)
        .limit(1);

      if (reservaAtivaError) {
        throw new Error(formatSupabaseError("Não foi possível validar a disponibilidade", reservaAtivaError));
      }

      if ((reservasConflitantes || []).length > 0) {
        setMessageType("error");
        setMessage("Esta posição já está reservada nesse intervalo de horário.");
        return;
      }

      const { error } = await supabase.from("reserva").insert({
        funcionario_id: funcionario.id,
        posicao_id: posicao.id,
        data_inicio: inicioReserva,
        data_fim: fimReserva,
        status: STATUS_RESERVA_ATIVA,
      });

      if (error) {
        throw new Error(formatSupabaseError("Não foi possível concluir a reserva", error));
      }

      setSalaSelecionada(null);
      setMessageType("success");
      setMessage("Reserva criada com sucesso.");
    } catch (error) {
      console.error(error);
      setMessageType("error");
      setMessage(error instanceof Error ? error.message : "Não foi possível concluir a reserva.");
    } finally {
      setIsReservando(false);
    }
  }

  function handleLogout() {
    clearFuncionarioSession();
    router.push("/");
  }

  const salasFiltradas = useMemo(() => {
    const termo = normalizarTexto(busca);

    return salas.filter((sala) => {
      if (!termo) {
        return true;
      }

      return normalizarTexto([sala.nome, sala.localizacao].join(" ")).includes(termo);
    });
  }, [busca, salas]);

  const quantidadeMesasPorSala = useMemo(() => {
    return mesas.reduce<Record<string, number>>((acc, mesa) => {
      if (mesa.sala_id) {
        const salaId = String(mesa.sala_id);
        acc[salaId] = (acc[salaId] || 0) + 1;
      }

      return acc;
    }, {});
  }, [mesas]);

  const quantidadePosicoesDisponiveis = useMemo(
    () =>
      posicoes.filter((posicao) => isPosicaoDisponivel(posicao, salaSelecionada)).length
      + posicoesSemSala.filter((posicao) => isPosicaoDisponivel(posicao, null)).length,
    [posicoes, posicoesSemSala, salaSelecionada]
  );

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <button className={styles.logoArea} type="button" onClick={() => setBusca("")}>
          <span className={styles.logoAccent}>&gt;</span>
          <span className={styles.logoText}>accenture</span>
        </button>

        <div className={styles.actions}>
          <div className={styles.userInfo}>
            <span className={styles.userLabel}>Usuário</span>
            <strong>{funcionario?.nome || "Visitante"}</strong>
          </div>
          {isAdministrador(funcionario) && (
            <button className={styles.adminBtn} type="button" onClick={() => router.push("/admin")}>
              Admin
            </button>
          )}
          <button className={styles.adminBtn} type="button" onClick={() => router.push("/minhas-reservas")}>
            Minhas reservas
          </button>
          <button className={styles.logoutBtn} type="button" onClick={handleLogout}>
            Sair
          </button>
        </div>
      </header>

      <main className={styles.main}>
        <section className={styles.hero}>
          <div>
            <p className={styles.eyebrow}>Reserva de posições</p>
            <h1 className={styles.title}>Escolha uma sala e reserve uma posição de trabalho</h1>
            <p className={styles.subtitle}>
              Selecione o ambiente, escolha uma posição e informe o horário para concluir a reserva.
            </p>
          </div>
        </section>

        {message && <p className={`${styles.message} ${styles[messageType]}`}>{message}</p>}

        <div className={styles.sectionHeader}>
          <div>
            <p className={styles.eyebrow}>Salas</p>
            <h2>Salas disponíveis</h2>
          </div>
          <button className={styles.refreshBtn} type="button" onClick={buscarSalas}>
            Atualizar
          </button>
        </div>

        <div className={styles.controlsPanel}>
          <div className={styles.searchArea}>
            <label className={styles.searchLabel} htmlFor="busca">
              Buscar
            </label>
            <input
              id="busca"
              type="text"
              placeholder="Buscar por nome ou localização"
              className={styles.searchInput}
              value={busca}
              onChange={(event) => setBusca(event.target.value)}
            />
          </div>

          <p className={styles.resultSummary}>
            {isLoading
              ? "Carregando..."
              : `${salasFiltradas.length} sala${salasFiltradas.length === 1 ? "" : "s"}`}
          </p>
        </div>

        {isLoading ? (
          <div className={styles.stateBox}>Carregando salas...</div>
        ) : salasFiltradas.length === 0 ? (
          <div className={styles.stateBox}>Nenhuma sala encontrada.</div>
        ) : (
          <div className={styles.grid}>
            {salasFiltradas.map((sala) => {
              const disponivel = isSalaDisponivel(sala);

              return (
                <article key={sala.id} className={styles.card}>
                  <div className={styles.cardContent}>
                    <div className={styles.cardHeader}>
                      <h2 className={styles.cardTitle}>{sala.nome || "Sala"}</h2>
                      <span className={`${styles.status} ${disponivel ? styles.available : styles.unavailable}`}>
                        {disponivel ? "Disponível" : "Indisponível"}
                      </span>
                    </div>

                    <p className={styles.info}>{sala.localizacao || "Localização não informada"}</p>
                    <p className={styles.info}>
                      {quantidadeMesasPorSala[String(sala.id)] || 0} mesa(s) cadastrada(s)
                    </p>
                    {!disponivel && <p className={styles.maintenanceReason}>{motivoIndisponibilidadeSala(sala)}</p>}

                    <div className={styles.cardButtons}>
                      <button
                        className={styles.reserveBtn}
                        type="button"
                        onClick={() => setSalaSelecionada(sala)}
                        disabled={!disponivel}
                      >
                        Ver posições
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </main>

      {salaSelecionada && (
        <div className={styles.modalOverlay} role="dialog" aria-modal="true">
          <div className={styles.modal}>
            <div className={styles.modalHeader}>
              <div>
                <p className={styles.eyebrow}>Posições</p>
                <h2>{salaSelecionada.nome || "Sala"}</h2>
              </div>
              <button className={styles.closeBtn} type="button" onClick={() => setSalaSelecionada(null)}>
                Fechar
              </button>
            </div>

            <p className={styles.modalLead}>
              {isLoadingPosicoes
                ? "Consultando posições..."
                : `${quantidadePosicoesDisponiveis} ${quantidadePosicoesDisponiveis === 1 ? "posição" : "posições"} encontrada${quantidadePosicoesDisponiveis === 1 ? "" : "s"}.`}
            </p>

            <div className={styles.scheduleGrid}>
              <label>
                Data
                <input type="date" value={dataReserva} onChange={(event) => setDataReserva(event.target.value)} />
              </label>
              <label>
                Início
                <input type="time" value={horaInicio} onChange={(event) => setHoraInicio(event.target.value)} />
              </label>
              <label>
                Fim
                <input type="time" value={horaFim} onChange={(event) => setHoraFim(event.target.value)} />
              </label>
            </div>

            <section className={styles.positionsSection}>
              {posicoesAviso && !isLoadingPosicoes && <div className={styles.positionsState}>{posicoesAviso}</div>}
              {isLoadingPosicoes ? (
                <div className={styles.positionsState}>Carregando posições...</div>
              ) : posicoes.length === 0 && posicoesSemSala.length === 0 ? (
                <div className={styles.positionsState}>
                  {posicoesAviso || "Nenhuma posição encontrada para esta sala."}
                </div>
              ) : (
                <div className={styles.positionsGrid}>
                  {posicoes.map((posicao) => {
                    const disponivel = isPosicaoDisponivel(posicao, salaSelecionada);
                    const motivo = motivoIndisponibilidadePosicao(posicao, salaSelecionada);
                    const equipamentos = equipamentosPorPosicao[String(posicao.id)] || [];

                    return (
                      <article key={posicao.id} className={styles.positionCard}>
                        <div>
                          <strong>{posicao.codigo_cadeira || `Posição ${posicao.id}`}</strong>
                          <small className={disponivel ? styles.availableText : styles.unavailableText}>
                            {disponivel ? "Disponível" : "Indisponível"}
                          </small>
                          {!disponivel && <p className={styles.maintenanceReason}>{motivo}</p>}
                        </div>

                        <div className={styles.equipmentList}>
                          {equipamentos.length > 0 ? (
                            equipamentos.map((equipamento) => (
                              <span key={equipamento.id}>
                                {equipamento.nome || equipamento.tipo || "Equipamento"}
                              </span>
                            ))
                          ) : (
                            <span>Sem equipamentos cadastrados</span>
                          )}
                        </div>

                        <button
                          className={styles.reserveBtn}
                          type="button"
                          onClick={() => handleReservarPosicao(posicao)}
                          disabled={!disponivel || isReservando}
                        >
                          {isReservando ? "Reservando..." : "Reservar"}
                        </button>
                      </article>
                    );
                  })}
                  {posicoesSemSala.length > 0 && (
                    <div className={styles.unlinkedPositions}>
                      <h3 className={styles.positionsSubheading}>Posições sem sala vinculada</h3>
                      <div className={styles.positionsGrid}>
                        {posicoesSemSala.map((posicao) => {
                          const disponivel = isPosicaoDisponivel(posicao, null);
                          const motivo = motivoIndisponibilidadePosicao(posicao, null);
                          const equipamentos = equipamentosPorPosicao[String(posicao.id)] || [];

                          return (
                            <article key={posicao.id} className={styles.positionCard}>
                              <div>
                                <strong>{posicao.codigo_cadeira || `Posição ${posicao.id}`}</strong>
                                <small className={disponivel ? styles.availableText : styles.unavailableText}>
                                  {disponivel ? "Disponível" : "Indisponível"}
                                </small>
                                {!disponivel && <p className={styles.maintenanceReason}>{motivo}</p>}
                              </div>

                              <div className={styles.equipmentList}>
                                {equipamentos.length > 0 ? (
                                  equipamentos.map((equipamento) => (
                                    <span key={equipamento.id}>
                                      {equipamento.nome || equipamento.tipo || "Equipamento"}
                                    </span>
                                  ))
                                ) : (
                                  <span>Sem equipamentos cadastrados</span>
                                )}
                              </div>

                              <button
                                className={styles.reserveBtn}
                                type="button"
                                onClick={() => handleReservarPosicao(posicao)}
                                disabled={!disponivel || isReservando}
                              >
                                {isReservando ? "Reservando..." : "Reservar"}
                              </button>
                            </article>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </section>

            <div className={styles.modalActions}>
              <button className={styles.detailsBtn} type="button" onClick={() => setSalaSelecionada(null)}>
                Voltar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}




