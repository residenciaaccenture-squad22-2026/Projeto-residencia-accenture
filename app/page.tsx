"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { isAdministrador, saveFuncionarioSession, type Funcionario } from "@/lib/session";
import { supabase } from "@/lib/supabase";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"error" | "success">("error");
  const [isLoading, setIsLoading] = useState(false);

  async function handleLogin() {
    setMessage("");

    if (!email.trim() || !password.trim()) {
      setMessageType("error");
      setMessage("Informe email e senha para continuar.");
      return;
    }

    setIsLoading(true);

    // Autenticação simplificada para apresentação acadêmica:
    // a senha é exigida no formulário, mas o acesso é validado pelo e-mail cadastrado no Supabase.
    const { data, error } = await supabase
      .from("funcionario")
      .select("id, nome, email, cargo, role")
      .eq("email", email.trim().toLowerCase())
      .maybeSingle<Funcionario>();

    setIsLoading(false);

    if (error) {
      console.error(error);
      setMessageType("error");
      setMessage("Não foi possível fazer login agora.");
      return;
    }

    if (!data) {
      setMessageType("error");
      setMessage("Usuário não encontrado.");
      return;
    }

    saveFuncionarioSession(data);
    setMessageType("success");
    setMessage("Login realizado com sucesso.");
    router.push(isAdministrador(data) ? "/admin" : "/home");
  }

  return (
    <div className="login-wrapper">
      <div className="left-panel">
        <div className="form-card">
          <h1 className="login-title">Login</h1>

          {message && <p className={`form-message ${messageType}`}>{message}</p>}

          <div className="field-group">
            <label className="field-label" htmlFor="email">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="field-input"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              placeholder="seu.email@empresa.com"
            />
          </div>

          <div className="field-group">
            <label className="field-label" htmlFor="password">
              Senha
            </label>
            <input
              id="password"
              type="password"
              className="field-input"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              placeholder="Senha para a apresentação"
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  handleLogin();
                }
              }}
            />
          </div>

          <button className="btn-entrar" type="button" onClick={handleLogin} disabled={isLoading}>
            {isLoading ? "Entrando..." : "Entrar"}
          </button>
        </div>
      </div>

      <div className="right-panel">
        <div className="brand-block">
          <div className="accenture-logo">
            <span className="accent-chevron">&gt;</span>
            <span className="accent-name">accenture</span>
          </div>
          <p className="register-hint">
            Não tem uma conta?
            <br />
            <strong>Crie seu acesso agora</strong>
          </p>
          <Link href="/cadastro" className="btn-cadastro">
            Cadastro
          </Link>
        </div>
      </div>
    </div>
  );
}
