"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { supabase } from "@/lib/supabase";

const perfis = [
  { cargo: "Funcionário", role: "FUNCIONARIO" },
  { cargo: "Gerente", role: "GESTOR" },
];

export default function CadastroPage() {
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [perfilIndex, setPerfilIndex] = useState("0");
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState<"error" | "success">("error");
  const [isLoading, setIsLoading] = useState(false);

  async function handleCadastro() {
    setMessage("");

    if (!name.trim() || !email.trim() || !password.trim()) {
      setMessageType("error");
      setMessage("Preencha nome, email e senha.");
      return;
    }

    if (password.trim().length < 6) {
      setMessageType("error");
      setMessage("Use uma senha com pelo menos 6 caracteres.");
      return;
    }

    const perfil = perfis[Number(perfilIndex)] || perfis[0];
    const normalizedEmail = email.trim().toLowerCase();
    setIsLoading(true);

    const { data: existingUser, error: lookupError } = await supabase
      .from("funcionario")
      .select("id")
      .eq("email", normalizedEmail)
      .maybeSingle();

    if (lookupError) {
      setIsLoading(false);
      console.error(lookupError);
      setMessageType("error");
      setMessage("Não foi possível validar o email.");
      return;
    }

    if (existingUser) {
      setIsLoading(false);
      setMessageType("error");
      setMessage("Este email já está cadastrado.");
      return;
    }

    const { error } = await supabase.from("funcionario").insert({
      nome: name.trim(),
      email: normalizedEmail,
      cargo: perfil.cargo,
      role: perfil.role,
    });

    setIsLoading(false);

    if (error) {
      console.error(error);
      setMessageType("error");
      setMessage("Erro ao cadastrar usuário.");
      return;
    }

    setMessageType("success");
    setMessage("Usuário cadastrado com sucesso.");
    router.push("/");
  }

  return (
    <div className="login-wrapper">
      <div className="left-panel">
        <div className="form-card">
          <h1 className="login-title">Cadastro</h1>

          {message && <p className={`form-message ${messageType}`}>{message}</p>}

          <div className="field-group">
            <label className="field-label" htmlFor="name">
              Nome
            </label>
            <input
              id="name"
              type="text"
              className="field-input"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="name"
              placeholder="Nome completo"
            />
          </div>

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
              autoComplete="new-password"
              placeholder="Mínimo de 6 caracteres"
            />
          </div>

          <div className="field-group">
            <label className="field-label" htmlFor="cargo">
              Tipo de usuário
            </label>
            <select
              id="cargo"
              className="field-select"
              value={perfilIndex}
              onChange={(event) => setPerfilIndex(event.target.value)}
            >
              {perfis.map((item, index) => (
                <option key={item.role} value={index}>
                  {item.cargo}
                </option>
              ))}
            </select>
          </div>

          <button className="btn-entrar" type="button" onClick={handleCadastro} disabled={isLoading}>
            {isLoading ? "Cadastrando..." : "Cadastrar"}
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
            Já tem uma conta?
            <br />
            <strong>Acesse sua área</strong>
          </p>
          <Link href="/" className="btn-cadastro">
            Fazer login
          </Link>
        </div>
      </div>
    </div>
  );
}
