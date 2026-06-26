"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useMemo, useState, useSyncExternalStore } from "react";
import { formatDate, formatSupabaseError, normalizarTexto, valueOrFallback } from "@/lib/app-utils";
import {
  clearFuncionarioSession,
  getFuncionarioServerSnapshot,
  getFuncionarioSnapshot,
  parseFuncionario,
  subscribeFuncionarioSnapshot,
} from "@/lib/session";
import { supabase } from "@/lib/supabase";
import styles from "./minhas-reservas.module.css";

const STATUS_RESERVA_CANCELADA = "CANCELADA";

type Reserva = {
  id: number | string;
  funcionario_id?: number | string | null;
  posicao_id?: number | string | null;
  data_inicio?: string | null;
  data_fim?: string | null;
  datafim?: string | null;
  datainicio?: string | null;
  status?: string | null;
  created_at?: string | null;
};

type Posicao = {
  id: number | string;
  sala_id?: number | string | null;
  codigo_cadeira?: string | null;
  coordenadas_x?: number | string | null;
  coordenadas_y?: number | string | null;
  disponivel?: boolean | null;
};

type Sala = {
  id: number | string;
  nome?: string | null;
  localizacao?: string | null;
};

function isReservaCancelada(status?: string | null) {
  return normalizarTexto(status) === "cancelada";
}

function statusLabel(status?: string | null) {
  if (isReservaCancelada(status)) {
    return "Cancelada";
  }

  return "Ativa";
}

export default function MinhasReservasPage() {
  const router = useRouter();
  const funcionarioSnapshot = useSyncExternalStore(
    subscribeFuncionarioSnapshot,
    getFuncionarioSnapshot,
    getFuncionarioServerSnapshot
  );
  const funcionario = useMemo(() => parseFuncionario(funcionarioSnapshot), [funcionarioSnapshot]);
  const [reservas, setReservas] = useState<Reserva[]>([]);
  const [posicoes, setPosicoes] = useState<Posicao[]>([]);
  const [salas, setSalas] = useState<Sala[]>([]);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"error" | "success">("success");
  const [isLoading, setIsLoading] = useState(false);
  const [reservaCancelandoId, setReservaCancelandoId] = useState<string | null>(null);

  const carregarReservas = useCallback(async () => {
    if (!funcionario) {
      return;
    }

    setIsLoading(true);
    setMessage("");

    const { data, error } = await supabase
      .from("reserva")
      .select("*")
      .eq("funcionario_id", funcionario.id)
      .order("created_at", { ascending: false });

    if (error) {
      console.error(error);
      setIsLoading(false);
      setMessageType("error");
      setMessage("Não foi possível carregar suas reservas.");
      return;
    }

    const reservasData = data || [];
    setReservas(reservasData);

    const posicaoIds = Array.from(new Set(reservasData.map((reserva) => reserva.posicao_id).filter(Boolean)));

    if (posicaoIds.length === 0) {
      setPosicoes([]);
      setSalas([]);
      setIsLoading(false);
      return;
    }

    const posicoesResult = await supabase
      .from("posicao")
      .select("id, sala_id, codigo_cadeira, coordenadas_x, coordenadas_y, disponivel")
      .in("id", posicaoIds);

    if (posicoesResult.error) {
      console.error(posicoesResult.error);
      setIsLoading(false);
      setMessageType("error");
      setMessage("Reservas carregadas, mas não foi possível carregar as posições.");
      return;
    }

    const posicoesData = posicoesResult.data || [];
    setPosicoes(posicoesData);

    const salaIds = Array.from(new Set(posicoesData.map((posicao) => posicao.sala_id).filter(Boolean)));

    if (salaIds.length === 0) {
      setSalas([]);
      setIsLoading(false);
      return;
    }

    const salasResult = await supabase.from("sala").select("id, nome, localizacao").in("id", salaIds);

    setIsLoading(false);

    if (salasResult.error) {
      console.error(salasResult.error);
      setMessageType("error");
      setMessage("Reservas carregadas, mas não foi possível carregar as salas.");
      return;
    }

    setSalas(salasResult.data || []);
  }, [funcionario]);

  useEffect(() => {
    if (funcionario) {
      const timeout = window.setTimeout(() => {
        carregarReservas();
      }, 0);

      return () => window.clearTimeout(timeout);
    }
  }, [funcionario, carregarReservas]);

  async function cancelarReserva(reserva: Reserva) {
    const confirmado = window.confirm("Cancelar esta reserva?");

    if (!confirmado) {
      return;
    }

    setReservaCancelandoId(String(reserva.id));
    setMessage("");

    const { error } = await supabase
      .from("reserva")
      .update({ status: STATUS_RESERVA_CANCELADA })
      .eq("id", reserva.id);

    setReservaCancelandoId(null);

    if (error) {
      console.error(error);
      setMessageType("error");
      setMessage(formatSupabaseError("Não foi possível cancelar a reserva", error));
      return;
    }

    setMessageType("success");
    setMessage("Reserva cancelada com sucesso.");
    carregarReservas();
  }

  const posicaoPorId = new Map(posicoes.map((posicao) => [String(posicao.id), posicao]));
  const salaPorId = new Map(salas.map((sala) => [String(sala.id), sala]));

  if (!funcionario) {
    return (
      <main className={styles.blocked}>
        <h1>Minhas reservas</h1>
        <p>Faça login para visualizar suas posições reservadas.</p>
        <button type="button" onClick={() => router.push("/")}>
          Ir para login
        </button>
      </main>
    );
  }

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <button className={styles.logoArea} type="button" onClick={() => router.push("/home")}>
          <span className={styles.logoAccent}>&gt;</span>
          <span className={styles.logoText}>accenture</span>
        </button>

        <div className={styles.actions}>
          <div>
            <span>Usuário</span>
            <strong>{funcionario.nome || funcionario.email}</strong>
          </div>
          <button type="button" onClick={() => router.push("/home")}>
            Home
          </button>
          <button
            type="button"
            onClick={() => {
              clearFuncionarioSession();
              router.push("/");
            }}
          >
            Sair
          </button>
        </div>
      </header>

      <main className={styles.main}>
        <section className={styles.hero}>
          <p className={styles.eyebrow}>Reservas</p>
          <h1>Minhas posições reservadas</h1>
          <p>Acompanhe suas reservas, consulte a sala e cancele uma solicitação quando necessário.</p>
        </section>

        {message && <p className={`${styles.message} ${styles[messageType]}`}>{message}</p>}

        {isLoading ? (
          <div className={styles.state}>Carregando reservas...</div>
        ) : reservas.length === 0 ? (
          <div className={styles.state}>Você ainda não possui posições reservadas.</div>
        ) : (
          <section className={styles.grid}>
            {reservas.map((reserva) => {
              const posicao = posicaoPorId.get(String(reserva.posicao_id));
              const sala = posicao ? salaPorId.get(String(posicao.sala_id)) : undefined;
              const cancelada = isReservaCancelada(reserva.status);
              const isCancelling = reservaCancelandoId === String(reserva.id);

              return (
                <article key={reserva.id} className={styles.card}>
                  <div className={styles.cardHeader}>
                    <div>
                      <p className={styles.eyebrow}>Reserva {reserva.id}</p>
                      <h2>{posicao?.codigo_cadeira || `Posição ${reserva.posicao_id}`}</h2>
                    </div>
                    <span className={cancelada ? styles.cancelled : styles.active}>
                      {statusLabel(reserva.status)}
                    </span>
                  </div>

                  <dl className={styles.details}>
                    <div>
                      <dt>Sala</dt>
                      <dd>{valueOrFallback(sala?.nome)}</dd>
                    </div>
                    <div>
                      <dt>Localização</dt>
                      <dd>{valueOrFallback(sala?.localizacao)}</dd>
                    </div>
                    <div>
                      <dt>Início</dt>
                      <dd>{formatDate(reserva.data_inicio || reserva.datainicio)}</dd>
                    </div>
                    <div>
                      <dt>Fim</dt>
                      <dd>{formatDate(reserva.data_fim || reserva.datafim)}</dd>
                    </div>
                    <div>
                      <dt>Coordenadas</dt>
                      <dd>
                        X: {valueOrFallback(posicao?.coordenadas_x)} | Y:{" "}
                        {valueOrFallback(posicao?.coordenadas_y)}
                      </dd>
                    </div>
                  </dl>

                  <button
                    type="button"
                    onClick={() => cancelarReserva(reserva)}
                    disabled={cancelada || isCancelling}
                  >
                    {isCancelling ? "Cancelando..." : "Cancelar reserva"}
                  </button>
                </article>
              );
            })}
          </section>
        )}
      </main>
    </div>
  );
}
