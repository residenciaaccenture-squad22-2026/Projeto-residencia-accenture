import { normalizarTexto } from "./app-utils";

export type Funcionario = {
  id: number | string;
  nome?: string | null;
  email?: string | null;
  cargo?: string | null;
  posicao_preferencial?: string | null;
  role?: string | null;
  created_at?: string | null;
};

const FUNCIONARIO_STORAGE_KEY = "funcionario";

export function subscribeFuncionarioSnapshot() {
  return () => {};
}

export function getFuncionarioSnapshot() {
  return localStorage.getItem(FUNCIONARIO_STORAGE_KEY) || "";
}

export function getFuncionarioServerSnapshot() {
  return "";
}

export function parseFuncionario(snapshot: string) {
  if (!snapshot) {
    return null;
  }

  try {
    return JSON.parse(snapshot) as Funcionario;
  } catch {
    clearFuncionarioSession();
    return null;
  }
}

export function saveFuncionarioSession(funcionario: Funcionario) {
  localStorage.setItem(FUNCIONARIO_STORAGE_KEY, JSON.stringify(funcionario));
}

export function clearFuncionarioSession() {
  localStorage.removeItem(FUNCIONARIO_STORAGE_KEY);
}

export function isAdministrador(funcionario: Funcionario | null) {
  const cargo = normalizarTexto(funcionario?.cargo);
  const role = normalizarTexto(funcionario?.role);

  return cargo === "administrador" || role === "administrador" || role === "admin";
}
