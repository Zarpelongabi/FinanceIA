# FinanceiroIA - Manual Técnico e de Funcionalidades 🚀

Bem-vindo ao **FinanceiroIA**, um ecossistema de gestão financeira "Eco Imersivo" focado em simplicidade, inteligência e privacidade.

---

## 🎨 Estética "Eco Imersivo"
O app utiliza uma paleta ultra-dark (`#0D0D0D`) com detalhes em verde eco e transparências modernas.
- **Ícones Personalizados:** O acesso ao patrimônio é feito via ícone exclusivo (`investimento.png`).
- **Cards Neon:** Interfaces de investimento com bordas iluminadas e profundidade.

---

## 💎 Funcionalidades Principais

### 1. Gestão de Patrimônio e Fluxo de Caixa 📈 (ATUALIZADO)
O módulo de **Investimentos** agora é interconectado com seu saldo mensal:
- **Fluxo de Sobra:** O app calcula automaticamente `(Salário + Renda Extra) - Gastos`. Se houver saldo positivo, o botão **"Investir Sobra"** permite guardar todo o valor restante com um toque.
- **Poupança Sicredi:** Simulação de rendimento baseada em 0.5% ao mês.
- **Outros Investimentos:** Área para aportes em CDB/Ações com simulação de 1% ao mês.
- **Guardar Dinheiro:** Termo simplificado para aportes, tornando a experiência mais intuitiva.
- **Reserva de Metas:** Exibe quanto do seu montante total na poupança já está comprometido com o progresso das suas metas.

### 2. Notificações Inteligentes (5º Dia Útil) 🔔
Implementado via `WorkManager`, o app monitora o calendário e dispara um alerta no dia anterior ao 5º dia útil.
- **Lógica:** Considera sábados como dias úteis (CLT) e respeita feriados.

### 3. Modo Ghost (Privacidade) 👁️‍🗨️
Ativado pelo botão de alternância no topo.
- **Funcionalidade:** Substitui todos os valores monetários por `****` em todo o app.

### 4. Inteligência de Renda 💰
- **Gestão Flexível:** Adição de Renda Extra com opção de **Zerar Acúmulo** para novos ciclos mensais.

### 5. Metas Gamificadas 🏆
Exibição em `RecyclerView` com progresso em tempo real, interligado ao montante guardado na poupança.

---

## 🛠️ Arquitetura Técnica

- **Banco de Dados:** Room v3 com suporte a consultas agregadas (`SUM`).
- **Entidades:** `Transacao`, `Meta`, `Categoria`.
- **Interconectividade:** `InvestimentosActivity` consome dados em tempo real do `TransactionDao` e `MetaDao` para calcular disponibilidades.

---

## 🗺️ Localização e Tradução
- Todo o código e interface estão em Português (Brasil).
- Moeda: BRL (R$).

---
*FinanceiroIA - Transformando números em liberdade.*
