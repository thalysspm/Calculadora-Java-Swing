import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculadora extends JFrame {

    private JTextField display;
    private String expressao = "";

    private static final String BTN_CLEAR     = "C";
    private static final String BTN_BACKSPACE = "BACK";
    private static final String BTN_SQRT      = "√";
    private static final String BTN_PERCENT   = "%";
    private static final String BTN_EQUALS    = "=";

    public Calculadora() {

        setTitle("Calculadora");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color fundo = new Color(15, 23, 42);
        getContentPane().setBackground(fundo);
        setLayout(new BorderLayout(10, 10));

        display = new JTextField();
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(new Font("Segoe UI", Font.BOLD, 40));
        display.setBackground(fundo);
        display.setForeground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(display, BorderLayout.NORTH);

        JPanel painel = new JPanel(new GridLayout(5, 4, 10, 10));
        painel.setBackground(fundo);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[][] botoes = {
            {"C","C"},   {"⌫","BACK"}, {"√","√"}, {"/","/"},
            {"7","7"},   {"8","8"},    {"9","9"},  {"*","*"},
            {"4","4"},   {"5","5"},    {"6","6"},  {"-","-"},
            {"1","1"},   {"2","2"},    {"3","3"},  {"+","+"},
            {"%","%"},   {"0","0"},    {".","."},  {"=","="}
        };

        for (String[] entrada : botoes) {
            String label   = entrada[0];
            String comando = entrada[1];

            JButton botao = new JButton(label);
            botao.setActionCommand(comando);
            botao.setFont(new Font("Segoe UI", Font.BOLD, 22));
            botao.setFocusPainted(false);
            botao.setBorderPainted(false);

            boolean ehEspecial = "/ * + - C BACK √ % =".contains(comando);
            Color corNormal = ehEspecial ? new Color(59,130,246) : new Color(30,41,59);
            Color corHover  = ehEspecial ? new Color(96,165,250) : new Color(51,65,85);

            botao.setBackground(corNormal);
            botao.setForeground(Color.WHITE);
            botao.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { botao.setBackground(corHover);  }
                @Override public void mouseExited (MouseEvent e) { botao.setBackground(corNormal); }
            });
            botao.addActionListener(e -> processarComando(e.getActionCommand()));
            painel.add(botao);
        }

        add(painel, BorderLayout.CENTER);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int  code = e.getKeyCode();
                char ch   = e.getKeyChar();
                if ((ch >= '0' && ch <= '9') || ch == '.')       processarComando(String.valueOf(ch));
                else if ("+-*/".indexOf(ch) >= 0)                 processarComando(String.valueOf(ch));
                else if (ch == '%')                               processarComando(BTN_PERCENT);
                else if (code == KeyEvent.VK_ENTER || ch == '=')  processarComando(BTN_EQUALS);
                else if (code == KeyEvent.VK_BACK_SPACE)          processarComando(BTN_BACKSPACE);
                else if (code == KeyEvent.VK_DELETE || ch == 'c' || ch == 'C') processarComando(BTN_CLEAR);
                else if (ch == 'r' || ch == 'R' || ch == 's' || ch == 'S')     processarComando(BTN_SQRT);
            }
        });

        setFocusable(true);
        requestFocusInWindow();
        setVisible(true);
    }

    // ── Lógica ───────────────────────────────────────────────────────────────

    private void processarComando(String cmd) {
        switch (cmd) {
            case BTN_CLEAR:
                expressao = "";
                display.setText("");
                break;

            case BTN_BACKSPACE:
                if (!expressao.isEmpty()) {
                    expressao = expressao.substring(0, expressao.length() - 1);
                    display.setText(expressao);
                }
                break;

            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
                expressao += cmd;
                display.setText(expressao);
                break;

            case ".":
                if (!segmentoTemPonto()) {
                    expressao += (expressao.isEmpty() || terminaEmOp()) ? "0." : ".";
                    display.setText(expressao);
                }
                break;

            case "+": case "-": case "*": case "/":
                if (expressao.isEmpty()) break;
                if (terminaEmOp())
                    expressao = expressao.substring(0, expressao.length() - 1) + cmd;
                else
                    expressao += cmd;
                display.setText(expressao);
                break;

            case BTN_PERCENT:
                expressao = aplicarPorcentagem(expressao);
                display.setText(expressao);
                break;

            case BTN_SQRT:
                expressao = aplicarRaiz(expressao);
                display.setText(expressao);
                break;

            case BTN_EQUALS:
                calcular();
                break;
        }
    }

    private void calcular() {
        if (expressao.isEmpty() || terminaEmOp()) { erro(); return; }
        try {
            double resultado = avaliar(expressao);
            expressao = formatar(resultado);
            display.setText(expressao);
        } catch (Exception ex) {
            erro();
        }
    }

    private void erro() {
        display.setText("Erro");
        expressao = "";
    }

    // ── Parser matemático (sem ScriptEngine) ─────────────────────────────────

    private int pos;
    private String expr;

    private double avaliar(String expressao) {
        this.expr = expressao;
        this.pos  = 0;
        double resultado = parseExpressao();
        if (pos != expr.length()) throw new RuntimeException("Expressão inválida");
        return resultado;
    }

    private double parseExpressao() {
        double valor = parseTermo();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '+') { pos++; valor += parseTermo(); }
            else if (c == '-') { pos++; valor -= parseTermo(); }
            else break;
        }
        return valor;
    }

    private double parseTermo() {
        double valor = parseFator();
        while (pos < expr.length()) {
            char c = expr.charAt(pos);
            if (c == '*') { pos++; valor *= parseFator(); }
            else if (c == '/') {
                pos++;
                double divisor = parseFator();
                if (divisor == 0) throw new ArithmeticException("Divisão por zero");
                valor /= divisor;
            }
            else break;
        }
        return valor;
    }

    private double parseFator() {
        if (pos < expr.length() && expr.charAt(pos) == '-') {
            pos++;
            return -parseFator();
        }
        int inicio = pos;
        if (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
            while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.'))
                pos++;
            return Double.parseDouble(expr.substring(inicio, pos));
        }
        throw new RuntimeException("Caractere inesperado na posição " + pos);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean terminaEmOp() {
        if (expressao.isEmpty()) return false;
        return "+-*/".indexOf(expressao.charAt(expressao.length() - 1)) >= 0;
    }

    private boolean segmentoTemPonto() {
        int i = expressao.length() - 1;
        while (i >= 0 && "+-*/".indexOf(expressao.charAt(i)) < 0) {
            if (expressao.charAt(i) == '.') return true;
            i--;
        }
        return false;
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
            if (v < 0) { erro(); return ""; }
            return e.substring(0, ini) + formatar(Math.sqrt(v));
        } catch (Exception ex) { return e; }
    }

    private int ultimoNumeroInicio(String e) {
        for (int i = e.length() - 1; i > 0; i--)
            if ("+-*/".indexOf(e.charAt(i)) >= 0) return i + 1;
        return 0;
    }

    private String formatar(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v))
            return String.valueOf((long) v);
        return String.valueOf(v);
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new Calculadora();
        });
    }
}