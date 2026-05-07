# 🎨 FinanceIA - Design System Minimalista

## 📱 O Projeto

Interface moderna de finanças pessoais com:
- **Estilo**: Minimalista + Glassmorphism
- **Paleta**: Azul Marinho (#1A3A6B) + Menta (#00D989)
- **Tipografia**: Roboto sans-serif
- **Status**: ✅ Alta Fidelidade

---

## 🎨 Cores Principais

| Uso | Cor | Hex |
|-----|-----|-----|
| Fundo | Navy Medium | `#1A3A6B` |
| CTA (Botões) | Mint | `#00D989` |
| Background Screen | Off White | `#F5F7FA` |
| Texto | Dark | `#1A2841` |
| Sucesso | Green | `#00D989` |
| Erro | Red | `#FF4757` |

---

## 📐 Componentes

### Dashboard
- **Card Saldo**: 180dp, Navy Medium, elevação 12dp, raio 24dp
- **Quick Stats**: 2 cards de 140dp (Gastos/Economia)
- **Transações**: Lista com 56dp altura por item

### Bottom Navigation
- **5 itens**: Dashboard, Histórico, Metas, Relatórios, Configurações
- **Altura**: 56dp
- **Estado**: Mint quando ativo, Gray quando inativo

### Tipografia
- **Heading 1**: 32sp bold
- **Body**: 16sp regular  
- **Caption**: 12sp regular

---

## 📁 Arquivos Essenciais

```
res/
├── values/
│   ├── colors.xml          (Paleta)
│   ├── styles.xml          (Estilos)
│   └── dimens.xml          (Dimensões)
├── layout/
│   ├── activity_main.xml
│   └── fragment_dashboard.xml
├── drawable/
│   ├── ic_*.xml            (11 ícones)
│   └── bg_icon_rounded.xml
├── color/
│   └── bottom_nav_color_state.xml
├── font/
│   ├── roboto.xml
│   ├── roboto_medium.xml
│   └── roboto_bold.xml
└── menu/
    └── bottom_nav_menu.xml
```

---

## 🚀 Quick Start

1. **Colors**: Use `@color/mint_medium` para CTAs
2. **Cards**: Use `style="@style/GlassmorphCard"`
3. **Text**: Use `style="@style/Body1"` ou `@style/Heading2`
4. **Icons**: Use drawables em `@drawable/ic_*`

---

## 💡 Design Principles

✅ Minimalismo  
✅ Consistência  
✅ Acessibilidade (WCAG AA)  
✅ Responsividade  
✅ Premium feel  

---

*Versão 1.0 • Projeto Limpo & Fluido*
