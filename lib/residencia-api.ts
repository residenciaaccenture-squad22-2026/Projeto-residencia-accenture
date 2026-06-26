const residenciaApiBaseUrl =
  process.env.NEXT_PUBLIC_RESIDENCIA_API_URL || "http://localhost:8080";

export type ResourceStatus = "DISPONIVEL" | "INDISPONIVEL" | "MANUTENCAO";
export type ReservationStatus = "ATIVA" | "CANCELADA";

export type ResidenciaStatus = {
  nome: string;
  status: string;
  dataHora: string;
  endpoints: string[];
};

export type ResidenciaEquipment = {
  id: number;
  nome: string;
  descricao: string | null;
  tipo: string;
  salaId: number | null;
  posicaoId: number | null;
  status: ResourceStatus;
};

export type ResidenciaRoom = {
  id: number;
  nome: string;
  capacidade: number;
  localizacao: string | null;
  status: ResourceStatus;
  equipamentos: ResidenciaEquipment[];
};

export type ResidenciaPosition = {
  id: number;
  codigo: string;
  descricao: string | null;
  localizacao: string | null;
  recursos: string | null;
  status: ResourceStatus;
  equipamentos: ResidenciaEquipment[];
};

export type ResidenciaUser = {
  id: number;
  nome: string;
  role: "FUNCIONARIO" | "GESTOR" | "ADMIN";
  cargo: string | null;
};

export type ResidenciaReservation = {
  id: number;
  sala: ResidenciaRoom | null;
  posicao: ResidenciaPosition | null;
  usuario: ResidenciaUser | null;
  responsavel: string;
  dataHoraInicio: string;
  dataHoraFim: string;
  status: ReservationStatus;
};

type RequestOptions = {
  signal?: AbortSignal;
};

type PlantAnalysisRequest = {
  imageUrl?: string;
  base64Image?: string;
  observacao?: string;
  roomNameHint?: string;
};

export type PlantAnalysisResponse = {
  sala?: string | null;
  totalPos?: number | null;
  pos?: Array<{
    cod?: string | null;
    lin?: number | null;
    col?: number | null;
    coord?: {
      x?: number | null;
      y?: number | null;
    } | null;
    eq?: Array<{
      t?: string | null;
      q?: number | null;
      c?: string | null;
    }> | null;
    conf?: string | null;
  }> | null;
  resumoEq?: Array<{
    t?: string | null;
    qtd?: number | null;
  }> | null;
  confGeral?: string | null;
  revisao?: boolean | null;
  obs?: string[] | null;
};

async function parseError(response: Response) {
  const errorBody = await response.json().catch(() => null);
  return errorBody?.mensagem || errorBody?.message || `Erro HTTP ${response.status}`;
}

async function request<T>(path: string, options: RequestOptions = {}) {
  const response = await fetch(`${residenciaApiBaseUrl}${path}`, {
    headers: {
      Accept: "application/json",
    },
    cache: "no-store",
    signal: options.signal,
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as T;
}

async function postJson<T>(path: string, body: unknown, options: RequestOptions = {}) {
  const response = await fetch(`${residenciaApiBaseUrl}${path}`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
    cache: "no-store",
    signal: options.signal,
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return (await response.json()) as T;
}

export const residenciaApi = {
  baseUrl: residenciaApiBaseUrl,

  getStatus(options?: RequestOptions) {
    return request<ResidenciaStatus>("/status", options);
  },

  getRooms(options?: RequestOptions) {
    return request<ResidenciaRoom[]>("/salas", options);
  },

  getPositions(options?: RequestOptions) {
    return request<ResidenciaPosition[]>("/posicoes", options);
  },

  getReservations(options?: RequestOptions) {
    return request<ResidenciaReservation[]>("/reservas", options);
  },

  getUsers(options?: RequestOptions) {
    return request<ResidenciaUser[]>("/usuarios", options);
  },

  analyzePlant(payload: PlantAnalysisRequest, options?: RequestOptions) {
    return postJson<PlantAnalysisResponse>("/agents/plant-analysis", payload, options);
  },
};
