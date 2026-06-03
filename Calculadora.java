import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;

public class Calculadora extends JFrame {

    // ── Estado ────────────────────────────────────────────────────────────────
    private String expressao = "";
    private double memoria   = 0;
    private boolean modoEscuro = true;
    private boolean modoCientifico = false;
    private final List<String> historico = new ArrayList<>();

    // ── Componentes principais ────────────────────────────────────────────────
    private JLabel  labelExpressao;
    private JLabel  labelResultado;
    private JPanel  painelBotoes;
    private JPanel  painelCientifico;
    private JPanel  painelHistorico;
    private JList<String> listaHistorico;
    private DefaultListModel<String> modeloHistorico;
    private JPanel  painelConversor;
    private JComboBox<String> comboDe, comboPara;
    private JTextField campoConversor;
    private JLabel labelConversorResultado;

    // ── Paletas ───────────────────────────────────────────────────────────────
    // Escuro
    private static final Color D_FUNDO      = new Color(15,  23,  42);
    private static final Color D_DISPLAY    = new Color(10,  16,  30);
    private static final Color D_BTN_NUM    = new Color(30,  41,  59);
    private static final Color D_BTN_OP     = new Color(59, 130, 246);
    private static final Color D_BTN_HOV_N  = new Color(51,  65,  85);
    private static final Color D_BTN_HOV_O  = new Color(96, 165, 250);
    private static final Color D_TEXTO      = Color.WHITE;
    private static final Color D_EXPR       = new Color(148,163,184);

    // Claro
    private static final Color L_FUNDO      = new Color(241,245,249);
    private static final Color L_DISPLAY    = new Color(255,255,255);
    private static final Color L_BTN_NUM    = new Color(255,255,255);
    private static final Color L_BTN_OP     = new Color(59, 130, 246);
    private static final Color L_BTN_HOV_N  = new Color(226,232,240);
    private static final Color L_BTN_HOV_O  = new Color(96, 165, 250);
    private static final Color L_TEXTO      = new Color(15,  23,  42);
    private static final Color L_EXPR       = new Color(100,116,139);

    // ── Constantes de comando ─────────────────────────────────────────────────
    private static final String C_CLEAR  = "C";
    private static final String C_BACK   = "BACK";
    private static final String C_SQRT   = "√";
    private static final String C_PCT    = "%";
    private static final String C_EQ     = "=";
    private static final String C_MC     = "MC";
    private static final String C_MR     = "MR";
    private static final String C_MPLUS  = "M+";
    private static final String C_MMINUS = "M-";
    private static final String C_SIN    = "sin";
    private static final String C_COS    = "cos";
    private static final String C_TAN    = "tan";
    private static final String C_LOG    = "log";
    private static final String C_LN     = "ln";
    private static final String C_SQ     = "x²";
    private static final String C_POW    = "xⁿ";
    private static final String C_PI     = "π";
    private static final String C_E      = "e";
    private static final String C_ABS    = "|x|";
    private static final String C_PARL   = "(";
    private static final String C_PARR   = ")";

    // ── Parser ────────────────────────────────────────────────────────────────
    private int    parserPos;
    private String parserExpr;

    // =========================================================================
    public Calculadora() {
        setTitle("Calculadora");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        construirUI();
        aplicarTema();
        configurarTeclado();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Construção da UI ──────────────────────────────────────────────────────
    private void construirUI() {
        setLayout(new BorderLayout(0, 0));

        // === PAINEL ESQUERDO (calculadora principal) ==========================
        JPanel painelEsquerdo = new JPanel(new BorderLayout(0, 0));

        // --- Display ----------------------------------------------------------
        JPanel display = new JPanel(new BorderLayout());
        display.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        labelExpressao = new JLabel(" ");
        labelExpressao.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        labelExpressao.setHorizontalAlignment(SwingConstants.RIGHT);

        labelResultado = new JLabel("0");
        labelResultado.setFont(new Font("Segoe UI", Font.BOLD, 48));
        labelResultado.setHorizontalAlignment(SwingConstants.RIGHT);

        display.add(labelExpressao, BorderLayout.NORTH);
        display.add(labelResultado, BorderLayout.CENTER);
        painelEsquerdo.add(display, BorderLayout.NORTH);

        // --- Botões de memória e utilitários ----------------------------------
        JPanel painelUtil = new JPanel(new GridLayout(1, 6, 6, 0));
        painelUtil.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        String[][] util = {
            {"MC",C_MC},{"MR",C_MR},{"M+",C_MPLUS},{"M-",C_MMINUS},
            {"( )",C_PARL},{"sci","SCI"}
        };
        for (String[] u : util) {
            JButton b = criarBotaoUtil(u[0], u[1]);
            painelUtil.add(b);
        }
        painelEsquerdo.add(painelUtil, BorderLayout.CENTER);

        // --- Grade principal --------------------------------------------------
        painelBotoes = new JPanel(new GridLayout(5, 4, 8, 8));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

        String[][] grade = {
            {"C","C"},     {"⌫","BACK"}, {"√","√"},  {"/","/"},
            {"7","7"},     {"8","8"},    {"9","9"},   {"*","*"},
            {"4","4"},     {"5","5"},    {"6","6"},   {"-","-"},
            {"1","1"},     {"2","2"},    {"3","3"},   {"+","+"},
            {"%","%"},     {"0","0"},    {".","."},   {"=","="}
        };
        for (String[] e : grade) painelBotoes.add(criarBotao(e[0], e[1]));

        JPanel wrapBotoes = new JPanel(new BorderLayout());
        wrapBotoes.add(painelUtil, BorderLayout.NORTH);
        wrapBotoes.add(painelBotoes, BorderLayout.CENTER);
        painelEsquerdo.add(wrapBotoes, BorderLayout.SOUTH);

        // === PAINEL CIENTÍFICO (oculto por padrão) ============================
        painelCientifico = new JPanel(new GridLayout(5, 2, 8, 8));
        painelCientifico.setBorder(BorderFactory.createEmptyBorder(8, 4, 10, 10));
        painelCientifico.setVisible(false);

        String[][] cientifico = {
            {"sin","sin"},{"cos","cos"},
            {"tan","tan"},{"log","log"},
            {"ln","ln"},  {"x²","x²"},
            {"xⁿ","xⁿ"}, {"π","π"},
            {"e","e"},    {"|x|","|x|"}
        };
        for (String[] c : cientifico) painelCientifico.add(criarBotaoCientifico(c[0], c[1]));

        // === PAINEL DIREITO (histórico + conversor) ===========================
        JPanel painelDireito = new JPanel(new BorderLayout(0, 8));
        painelDireito.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        painelDireito.setPreferredSize(new Dimension(200, 0));

        // Botão tema
        JButton btnTema = new JButton("☀ / ☾");
        btnTema.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnTema.setFocusPainted(false);
        btnTema.setBorderPainted(false);
        btnTema.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTema.addActionListener(e -> { modoEscuro = !modoEscuro; aplicarTema(); });
        painelDireito.add(btnTema, BorderLayout.NORTH);

        // Histórico
        modeloHistorico = new DefaultListModel<>();
        listaHistorico  = new JList<>(modeloHistorico);
        listaHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaHistorico.setCellRenderer(new HistoricoCellRenderer());
        listaHistorico.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = listaHistorico.getSelectedValue();
                    if (sel != null) {
                        String[] parts = sel.split("=");
                        if (parts.length == 2) {
                            expressao = parts[1].trim();
                            atualizarDisplay();
                        }
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(listaHistorico);
        scroll.setBorder(BorderFactory.createTitledBorder("Histórico"));

        // Conversor
        painelConversor = construirConversor();

        painelDireito.add(scroll,          BorderLayout.CENTER);
        painelDireito.add(painelConversor, BorderLayout.SOUTH);

        // === LAYOUT FINAL =====================================================
        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.add(painelCientifico, BorderLayout.WEST);
        centro.add(painelEsquerdo,   BorderLayout.CENTER);

        add(centro,        BorderLayout.CENTER);
        add(painelDireito, BorderLayout.EAST);
    }

    // ── Conversor de unidades ─────────────────────────────────────────────────
    private JPanel construirConversor() {
        JPanel p = new JPanel(new GridLayout(5, 1, 4, 4));
        p.setBorder(BorderFactory.createTitledBorder("Conversor"));

        String[] unidades = {"km","mi","°C","°F","kg","lb","m","ft"};
        comboDe  = new JComboBox<>(unidades);
        comboPara = new JComboBox<>(unidades);
        comboPara.setSelectedIndex(1);

        campoConversor = new JTextField("0");
        campoConversor.setHorizontalAlignment(JTextField.RIGHT);
        campoConversor.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        labelConversorResultado = new JLabel("= 0", SwingConstants.RIGHT);
        labelConversorResultado.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnConverter = new JButton("Converter");
        btnConverter.setFocusPainted(false);
        btnConverter.addActionListener(e -> converter());

        p.add(comboDe);
        p.add(comboPara);
        p.add(campoConversor);
        p.add(btnConverter);
        p.add(labelConversorResultado);
        return p;
    }

    private void converter() {
        try {
            double val  = Double.parseDouble(campoConversor.getText().replace(",","."));
            String de   = (String) comboDe.getSelectedItem();
            String para = (String) comboPara.getSelectedItem();
            double res  = converterValor(val, de, para);
            labelConversorResultado.setText("= " + formatar(res) + " " + para);
        } catch (Exception ex) {
            labelConversorResultado.setText("Erro");
        }
    }

    private double converterValor(double v, String de, String para) {
        double si;
        switch (de) {
            case "km": si = v * 1000; break;
            case "mi": si = v * 1609.344; break;
            case "°C": si = v; break;
            case "°F": si = (v - 32) * 5.0/9.0; break;
            case "kg": si = v; break;
            case "lb": si = v * 0.453592; break;
            case "m":  si = v; break;
            case "ft": si = v * 0.3048; break;
            default:   si = v;
        }
        switch (para) {
            case "km": return si / 1000;
            case "mi": return si / 1609.344;
            case "°C": return si;
            case "°F": return si * 9.0/5.0 + 32;
            case "kg": return si;
            case "lb": return si / 0.453592;
            case "m":  return si;
            case "ft": return si / 0.3048;
            default:   return si;
        }
    }

    // ── Fábrica de botões ─────────────────────────────────────────────────────
    private JButton criarBotao(String label, String cmd) {
        JButton b = new JButton(label);
        b.setActionCommand(cmd);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(75, 65));
        b.addActionListener(e -> { tocarClick(); processarComando(e.getActionCommand()); });
        return b;
    }

    private JButton criarBotaoCientifico(String label, String cmd) {
        JButton b = new JButton(label);
        b.setActionCommand(cmd);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(60, 55));
        b.addActionListener(e -> { tocarClick(); processarComando(e.getActionCommand()); });
        return b;
    }

    private JButton criarBotaoUtil(String label, String cmd) {
        JButton b = new JButton(label);
        b.setActionCommand(cmd);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(0, 36));
        b.addActionListener(e -> { tocarClick(); processarComando(e.getActionCommand()); });
        return b;
    }

    // ── Tema ──────────────────────────────────────────────────────────────────
    private void aplicarTema() {
        Color fundo   = modoEscuro ? D_FUNDO   : L_FUNDO;
        Color display = modoEscuro ? D_DISPLAY : L_DISPLAY;
        Color btnNum  = modoEscuro ? D_BTN_NUM : L_BTN_NUM;
        Color btnOp   = modoEscuro ? D_BTN_OP  : L_BTN_OP;
        Color hovNum  = modoEscuro ? D_BTN_HOV_N : L_BTN_HOV_N;
        Color hovOp   = modoEscuro ? D_BTN_HOV_O : L_BTN_HOV_O;
        Color texto   = modoEscuro ? D_TEXTO   : L_TEXTO;
        Color expr    = modoEscuro ? D_EXPR    : L_EXPR;

        getContentPane().setBackground(fundo);

        labelExpressao.setForeground(expr);
        labelResultado.setForeground(texto);
        labelExpressao.getParent().setBackground(display);

        listaHistorico.setBackground(display);
        listaHistorico.setForeground(texto);
        listaHistorico.getParent().getParent().setBackground(fundo);
        painelConversor.setBackground(fundo);
        comboDe.setBackground(display);    comboDe.setForeground(texto);
        comboPara.setBackground(display);  comboPara.setForeground(texto);
        campoConversor.setBackground(display); campoConversor.setForeground(texto);
        labelConversorResultado.setForeground(texto);

        colorirBotoes(painelBotoes,     btnNum, btnOp, hovNum, hovOp, texto);
        colorirBotoes(painelCientifico, btnOp,  btnOp, hovOp,  hovOp, texto);
        colorirBotoes(
            (JPanel) ((BorderLayout)((JPanel)painelBotoes.getParent()).getLayout())
                .getLayoutComponent(BorderLayout.NORTH),
            new Color(51,65,85), new Color(51,65,85),
            new Color(71,85,105), new Color(71,85,105), texto
        );

        painelBotoes.setBackground(fundo);
        painelCientifico.setBackground(fundo);
        painelBotoes.getParent().setBackground(fundo);

        repaint();
    }

    private void colorirBotoes(JPanel painel,
                                Color cn, Color co, Color hn, Color ho, Color texto) {
        if (painel == null) return;
        for (Component c : painel.getComponents()) {
            if (!(c instanceof JButton)) continue;
            JButton b   = (JButton) c;
            String  cmd = b.getActionCommand();
            boolean op  = ehBotaoOp(cmd);
            Color corN  = op ? co : cn;
            Color corH  = op ? ho : hn;
            b.setBackground(corN);
            b.setForeground(texto);
            for (MouseListener ml : b.getMouseListeners())
                if (ml instanceof HoverListener) b.removeMouseListener(ml);
            b.addMouseListener(new HoverListener(b, corN, corH));
        }
    }

    private boolean ehBotaoOp(String cmd) {
        return "/ * + - C BACK √ % = MC MR M+ M- SCI ( ) sin cos tan log ln x² xⁿ π e |x|".contains(cmd);
    }

    // ── Teclado ───────────────────────────────────────────────────────────────
    private void configurarTeclado() {
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int  code = e.getKeyCode();
                char ch   = e.getKeyChar();
                if ((ch >= '0' && ch <= '9') || ch == '.')       processarComando(String.valueOf(ch));
                else if ("+-*/".indexOf(ch) >= 0)                 processarComando(String.valueOf(ch));
                else if (ch == '%')                               processarComando(C_PCT);
                else if (code == KeyEvent.VK_ENTER || ch == '=')  processarComando(C_EQ);
                else if (code == KeyEvent.VK_BACK_SPACE)          processarComando(C_BACK);
                else if (code == KeyEvent.VK_DELETE || ch=='c' || ch=='C') processarComando(C_CLEAR);
                else if (ch == 'r' || ch == 'R' || ch == 's' || ch == 'S') processarComando(C_SQRT);
                else if (ch == '(')                               processarComando(C_PARL);
                else if (ch == ')')                               processarComando(C_PARR);
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    // ── Processamento de comandos ─────────────────────────────────────────────
    private void processarComando(String cmd) {
        switch (cmd) {
            case C_CLEAR:
                expressao = "";
                labelExpressao.setText(" ");
                labelResultado.setText("0");
                return;

            case C_BACK:
                if (!expressao.isEmpty()) {
                    expressao = expressao.substring(0, expressao.length() - 1);
                    atualizarDisplay();
                }
                return;

            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
                expressao += cmd;
                atualizarDisplay();
                return;

            case ".":
                if (!segmentoTemPonto()) {
                    expressao += (expressao.isEmpty() || terminaEmOp()) ? "0." : ".";
                    atualizarDisplay();
                }
                return;

            case "+": case "-": case "*": case "/":
                if (expressao.isEmpty()) return;
                if (terminaEmOp())
                    expressao = expressao.substring(0, expressao.length() - 1) + cmd;
                else
                    expressao += cmd;
                atualizarDisplay();
                return;

            case C_PARL:
                long abre   = expressao.chars().filter(c -> c == '(').count();
                long fecha  = expressao.chars().filter(c -> c == ')').count();
                boolean ultimo = !expressao.isEmpty() &&
                    (Character.isDigit(expressao.charAt(expressao.length()-1))
                        || expressao.charAt(expressao.length()-1) == ')');
                if (abre > fecha && ultimo)
                    expressao += ")";
                else
                    expressao += "(";
                atualizarDisplay();
                return;

            case C_PARR:
                expressao += ")";
                atualizarDisplay();
                return;

            case C_PCT:
                expressao = aplicarPorcentagem(expressao);
                atualizarDisplay();
                return;

            case C_SQRT:
                expressao = aplicarRaiz(expressao);
                atualizarDisplay();
                return;

            case C_SIN: expressao = aplicarFunc("sin"); atualizarDisplay(); return;
            case C_COS: expressao = aplicarFunc("cos"); atualizarDisplay(); return;
            case C_TAN: expressao = aplicarFunc("tan"); atualizarDisplay(); return;
            case C_LOG: expressao = aplicarFunc("log"); atualizarDisplay(); return;
            case C_LN:  expressao = aplicarFunc("ln");  atualizarDisplay(); return;
            case C_SQ:  expressao = aplicarQuadrado();  atualizarDisplay(); return;
            case C_POW: expressao += "^"; atualizarDisplay(); return;
            case C_PI:  expressao += "3.14159265358979"; atualizarDisplay(); return;
            case C_E:   expressao += "2.71828182845905"; atualizarDisplay(); return;
            case C_ABS: expressao = aplicarAbs(); atualizarDisplay(); return;

            case C_MC:    memoria = 0; return;
            case C_MR:    expressao += formatar(memoria); atualizarDisplay(); return;
            case C_MPLUS:
                try { memoria += avaliar(expressao); } catch (Exception ignored) {}
                return;
            case C_MMINUS:
                try { memoria -= avaliar(expressao); } catch (Exception ignored) {}
                return;

            case "SCI":
                modoCientifico = !modoCientifico;
                painelCientifico.setVisible(modoCientifico);
                pack();
                return;

            case C_EQ:
                calcular();
                return;
        }
    }

    // ── Cálculo ───────────────────────────────────────────────────────────────
    private void calcular() {
        if (expressao.isEmpty()) return;
        String exprParaCalc = expressao;
        try {
            double resultado = avaliar(exprParaCalc);
            String entrada   = exprParaCalc + " = " + formatar(resultado);
            historico.add(0, entrada);
            modeloHistorico.add(0, entrada);

            animarResultado(formatar(resultado));

            labelExpressao.setText(exprParaCalc + " =");
            expressao = formatar(resultado);
        } catch (Exception ex) {
            labelResultado.setText("Erro");
            expressao = "";
        }
    }

    private void animarResultado(String novo) {
        Timer t = new Timer(16, null);
        final float[] alpha = {0f};
        t.addActionListener(e -> {
            alpha[0] += 0.08f;
            if (alpha[0] >= 1f) { alpha[0] = 1f; t.stop(); }
            labelResultado.setForeground(
                blend(modoEscuro ? D_EXPR : L_EXPR,
                      modoEscuro ? D_TEXTO : L_TEXTO, alpha[0])
            );
            labelResultado.setText(novo);
        });
        t.start();
    }

    private Color blend(Color a, Color b, float t) {
        int r = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl= (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        return new Color(r, g, bl);
    }

    private void atualizarDisplay() {
        labelExpressao.setText(expressao.isEmpty() ? " " : expressao);
        if (!expressao.isEmpty() && !terminaEmOp()
                && expressao.indexOf('^') < 0) {
            try {
                double prev = avaliar(expressao);
                labelResultado.setText(formatar(prev));
            } catch (Exception ignored) {
                labelResultado.setText(expressao);
            }
        } else {
            labelResultado.setText(expressao.isEmpty() ? "0" : expressao);
        }
    }

    // ── Funções científicas ───────────────────────────────────────────────────
    private String aplicarFunc(String fn) {
        int ini = ultimoNumeroInicio(expressao);
        String pre = expressao.substring(0, ini);
        try {
            double v = Double.parseDouble(expressao.substring(ini));
            double r;
            switch (fn) {
                case "sin": r = Math.sin(Math.toRadians(v)); break;
                case "cos": r = Math.cos(Math.toRadians(v)); break;
                case "tan": r = Math.tan(Math.toRadians(v)); break;
                case "log": r = Math.log10(v); break;
                case "ln":  r = Math.log(v);   break;
                default:    r = v;
            }
            return pre + formatar(r);
        } catch (Exception e) { return expressao; }
    }

    private String aplicarQuadrado() {
        int ini = ultimoNumeroInicio(expressao);
        String pre = expressao.substring(0, ini);
        try {
            double v = Double.parseDouble(expressao.substring(ini));
            return pre + formatar(v * v);
        } catch (Exception e) { return expressao; }
    }

    private String aplicarAbs() {
        int ini = ultimoNumeroInicio(expressao);
        String pre = expressao.substring(0, ini);
        try {
            double v = Double.parseDouble(expressao.substring(ini));
            return pre + formatar(Math.abs(v));
        } catch (Exception e) { return expressao; }
    }

    private String aplicarPorcentagem(String e) {
        if (e.isEmpty()) return e;
        try {
            int ini = ultimoNumeroInicio(e);
            double v = Double.parseDouble(e.substring(ini)) / 100;
            return e.substring(0, ini) + formatar(v);
        } catch (Exception ex) { return e; }
    }

    private String aplicarRaiz(String e) {
        if (e.isEmpty()) return e;
        try {
            int ini = ultimoNumeroInicio(e);
            double v = Double.parseDouble(e.substring(ini));
            if (v < 0) { labelResultado.setText("Erro"); return ""; }
            return e.substring(0, ini) + formatar(Math.sqrt(v));
        } catch (Exception ex) { return e; }
    }

    // ── Parser ────────────────────────────────────────────────────────────────
    private double avaliar(String e) {
        parserExpr = e.trim();
        parserPos  = 0;
        double r = parseExpr();
        if (parserPos != parserExpr.length())
            throw new RuntimeException("Inválido");
        return r;
    }

    private double parseExpr() {
        double v = parseTerm();
        while (parserPos < parserExpr.length()) {
            char c = parserExpr.charAt(parserPos);
            if      (c == '+') { parserPos++; v += parseTerm(); }
            else if (c == '-') { parserPos++; v -= parseTerm(); }
            else break;
        }
        return v;
    }

    private double parseTerm() {
        double v = parsePow();
        while (parserPos < parserExpr.length()) {
            char c = parserExpr.charAt(parserPos);
            if (c == '*') { parserPos++; v *= parsePow(); }
            else if (c == '/') {
                parserPos++;
                double d = parsePow();
                if (d == 0) throw new ArithmeticException("Divisão por zero");
                v /= d;
            } else break;
        }
        return v;
    }

    private double parsePow() {
        double base = parseFactor();
        if (parserPos < parserExpr.length() && parserExpr.charAt(parserPos) == '^') {
            parserPos++;
            double exp = parseFactor();
            return Math.pow(base, exp);
        }
        return base;
    }

    private double parseFactor() {
        if (parserPos < parserExpr.length() && parserExpr.charAt(parserPos) == '-') {
            parserPos++;
            return -parseFactor();
        }
        if (parserPos < parserExpr.length() && parserExpr.charAt(parserPos) == '(') {
            parserPos++;
            double v = parseExpr();
            if (parserPos < parserExpr.length() && parserExpr.charAt(parserPos) == ')')
                parserPos++;
            return v;
        }
        int ini = parserPos;
        while (parserPos < parserExpr.length() &&
               (Character.isDigit(parserExpr.charAt(parserPos)) || parserExpr.charAt(parserPos) == '.'))
            parserPos++;
        if (parserPos == ini) throw new RuntimeException("Fator inválido em " + parserPos);
        return Double.parseDouble(parserExpr.substring(ini, parserPos));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean terminaEmOp() {
        if (expressao.isEmpty()) return false;
        char last = expressao.charAt(expressao.length() - 1);
        return "+-*/^".indexOf(last) >= 0;
    }

    private boolean segmentoTemPonto() {
        for (int i = expressao.length() - 1; i >= 0; i--) {
            char c = expressao.charAt(i);
            if ("+-*/^(".indexOf(c) >= 0) break;
            if (c == '.') return true;
        }
        return false;
    }

    private int ultimoNumeroInicio(String e) {
        for (int i = e.length() - 1; i > 0; i--)
            if ("+-*/^".indexOf(e.charAt(i)) >= 0) return i + 1;
        return 0;
    }

    private String formatar(double v) {
        if (Double.isNaN(v))      return "Erro";
        if (Double.isInfinite(v)) return "∞";
        if (v == Math.floor(v) && Math.abs(v) < 1e15)
            return String.valueOf((long) v);
        return String.format("%.10f", v).replaceAll("0+$","").replaceAll("\\.$","");
    }

    // ── Som de clique ─────────────────────────────────────────────────────────
    private void tocarClick() {
        try {
            byte[] buf = new byte[800];
            for (int i = 0; i < buf.length; i++)
                buf[i] = (byte)(Math.sin(2 * Math.PI * i * 880.0 / 44100) * 20);
            AudioFormat fmt = new AudioFormat(44100, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, buf.length);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Exception ignored) {}
    }

    // ── Listener de hover reutilizável ────────────────────────────────────────
    private static class HoverListener extends MouseAdapter {
        private final JButton btn;
        private final Color normal, hover;
        HoverListener(JButton b, Color n, Color h) { btn=b; normal=n; hover=h; }
        @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover);  }
        @Override public void mouseExited (MouseEvent e) { btn.setBackground(normal); }
    }

    // ── Renderer do histórico ─────────────────────────────────────────────────
    private class HistoricoCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int idx, boolean sel, boolean focus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list,value,idx,sel,focus);
            l.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            if (!sel) {
                l.setBackground(idx % 2 == 0
                    ? (modoEscuro ? D_DISPLAY : L_DISPLAY)
                    : (modoEscuro ? D_BTN_NUM : L_BTN_NUM));
                l.setForeground(modoEscuro ? D_TEXTO : L_TEXTO);
            }
            return l;
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new Calculadora();
        });
    }
}