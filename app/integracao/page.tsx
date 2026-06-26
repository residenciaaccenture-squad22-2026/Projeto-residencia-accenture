import Link from "next/link";
import { residenciaApi } from "@/lib/residencia-api";
import styles from "./integracao.module.css";

export const dynamic = "force-dynamic";

type IntegrationData = {
  online: boolean;
  error?: string;
  status?: Awaited<ReturnType<typeof residenciaApi.getStatus>>;
  rooms: Awaited<ReturnType<typeof residenciaApi.getRooms>>;
  positions: Awaited<ReturnType<typeof residenciaApi.getPositions>>;
  reservations: Awaited<ReturnType<typeof residenciaApi.getReservations>>;
  users: Awaited<ReturnType<typeof residenciaApi.getUsers>>;
};

async function loadIntegrationData(): Promise<IntegrationData> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 5000);

  try {
    const [status, rooms, positions, reservations, users] = await Promise.all([
      residenciaApi.getStatus({ signal: controller.signal }),
      residenciaApi.getRooms({ signal: controller.signal }),
      residenciaApi.getPositions({ signal: controller.signal }),
      residenciaApi.getReservations({ signal: controller.signal }),
      residenciaApi.getUsers({ signal: controller.signal }),
    ]);

    return {
      online: true,
      status,
      rooms,
      positions,
      reservations,
      users,
    };
  } catch (error) {
    return {
      online: false,
      error: error instanceof Error ? error.message : "Falha desconhecida",
      rooms: [],
      positions: [],
      reservations: [],
      users: [],
    };
  } finally {
    clearTimeout(timeout);
  }
}

export default async function IntegracaoPage() {
  const data = await loadIntegrationData();
  const activeReservations = data.reservations.filter(
    (reservation) => reservation.status === "ATIVA"
  ).length;

  return (
    <main className={styles.page}>
      <section className={styles.header}>
        <div>
          <p className={styles.eyebrow}>Integracao</p>
          <h1>Projeto Residencia Accenture</h1>
          <p>
            Conexao deste Next.js com a API Quarkus em{" "}
            <strong>{residenciaApi.baseUrl}</strong>.
          </p>
        </div>

        <Link className={styles.backLink} href="/home">
          Voltar
        </Link>
      </section>

      <section className={data.online ? styles.statusOnline : styles.statusOffline}>
        <span>{data.online ? "Online" : "Offline"}</span>
        <div>
          <strong>{data.status?.nome || "residencia-accenture-api"}</strong>
          <p>
            {data.online
              ? `API respondeu com status ${data.status?.status || "online"}.`
              : `Nao foi possivel conectar: ${data.error}`}
          </p>
        </div>
      </section>

      <section className={styles.metricsGrid} aria-label="Resumo da API integrada">
        <article>
          <span>Salas</span>
          <strong>{data.rooms.length}</strong>
        </article>
        <article>
          <span>Posicoes</span>
          <strong>{data.positions.length}</strong>
        </article>
        <article>
          <span>Usuarios</span>
          <strong>{data.users.length}</strong>
        </article>
        <article>
          <span>Reservas ativas</span>
          <strong>{activeReservations}</strong>
        </article>
      </section>

      <section className={styles.contentGrid}>
        <div className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Salas da API</h2>
            <span>{data.rooms.length}</span>
          </div>

          {data.rooms.length === 0 ? (
            <p className={styles.empty}>Nenhuma sala retornada pela API.</p>
          ) : (
            <ul className={styles.list}>
              {data.rooms.slice(0, 5).map((room) => (
                <li key={room.id}>
                  <strong>{room.nome}</strong>
                  <span>
                    {room.localizacao || "Sem localizacao"} | {room.capacidade} lugares |{" "}
                    {room.status}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className={styles.panel}>
          <div className={styles.panelHeader}>
            <h2>Reservas da API</h2>
            <span>{data.reservations.length}</span>
          </div>

          {data.reservations.length === 0 ? (
            <p className={styles.empty}>Nenhuma reserva retornada pela API.</p>
          ) : (
            <ul className={styles.list}>
              {data.reservations.slice(0, 5).map((reservation) => (
                <li key={reservation.id}>
                  <strong>{reservation.responsavel}</strong>
                  <span>
                    {reservation.sala?.nome ||
                      reservation.posicao?.codigo ||
                      "Recurso sem nome"}{" "}
                    | {reservation.status}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
    </main>
  );
}
