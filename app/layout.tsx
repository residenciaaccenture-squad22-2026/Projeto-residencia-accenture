import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Reserva de Posições",
  description: "Sistema interno para reserva de salas, mesas e posições de trabalho.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="pt-BR"
      className="h-full antialiased"
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
