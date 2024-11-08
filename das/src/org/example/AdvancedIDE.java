package org.example;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import javax.swing.event.*;

public class AdvancedIDE extends JFrame {
    private JTabbedPane tabbedPane;
    private JButton runButton, saveButton, newFileButton, copyButton, pasteButton, clearButton, createHtmlButton, toggleThemeButton;
    private JMenuBar menuBar;
    private JFileChooser fileChooser;
    private JToolBar toolBar;
    private JComboBox<String> fileTypeComboBox;
    private final String[] fileTypes = {"PHP", "HTML", "JavaScript", "CSS"};
    private File currentFile;
    private boolean isDarkMode = false;

    public AdvancedIDE() {
        setTitle("Advanced IDE");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createToolBar();
        createMenuBar();
        createTabbedEditor();
        createFileTypeComboBox();
        createFileChooser();
        applyTheme();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyShortcuts(e);
            }

            private void handleKeyShortcuts(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                    saveCode();  // Salvar (Ctrl+S)
                } else if (e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
                    createNewFile();  // Novo Arquivo (Ctrl+N)
                } else if (e.getKeyCode() == KeyEvent.VK_O && e.isControlDown()) {
                    openFile();  // Abrir Arquivo (Ctrl+O)
                }
            }
        });
        setFocusable(true);
    }

    private void createToolBar() {
        toolBar = new JToolBar("Ferramentas");
        toolBar.setFloatable(false);

        runButton = new JButton("Executar");
        saveButton = new JButton("Salvar");
        newFileButton = new JButton("Novo Arquivo");
        copyButton = new JButton("Copiar");
        pasteButton = new JButton("Colar");
        clearButton = new JButton("Limpar");
        createHtmlButton = new JButton("Criar HTML");
        toggleThemeButton = new JButton("Alternar Tema");

        runButton.addActionListener(e -> runCode());
        saveButton.addActionListener(e -> saveCode());
        newFileButton.addActionListener(e -> createNewFile());
        copyButton.addActionListener(e -> copyCode());
        pasteButton.addActionListener(e -> pasteCode());
        clearButton.addActionListener(e -> clearCode());
        createHtmlButton.addActionListener(e -> createHtmlTemplate());
        toggleThemeButton.addActionListener(e -> toggleTheme());

        toolBar.add(runButton);
        toolBar.add(saveButton);
        toolBar.add(newFileButton);
        toolBar.add(copyButton);
        toolBar.add(pasteButton);
        toolBar.add(clearButton);
        toolBar.add(createHtmlButton);
        toolBar.add(toggleThemeButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem newItem = new JMenuItem("Novo");
        newItem.addActionListener(e -> createNewFile());
        JMenuItem openItem = new JMenuItem("Abrir");
        openItem.addActionListener(e -> openFile());
        JMenuItem saveItem = new JMenuItem("Salvar");
        saveItem.addActionListener(e -> saveCode());
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);

        JMenu shortcutsMenu = new JMenu("Atalhos");
        JMenuItem shortcutSave = new JMenuItem("Salvar (Ctrl+S)");
        JMenuItem shortcutNewFile = new JMenuItem("Novo Arquivo (Ctrl+N)");
        JMenuItem shortcutOpenFile = new JMenuItem("Abrir Arquivo (Ctrl+O)");
        shortcutsMenu.add(shortcutSave);
        shortcutsMenu.add(shortcutNewFile);
        shortcutsMenu.add(shortcutOpenFile);

        menuBar.add(fileMenu);
        menuBar.add(shortcutsMenu);
        setJMenuBar(menuBar);
    }

    private void createTabbedEditor() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                int index = tabbedPane.getTabCount() - 1;
                JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                tabHeader.setOpaque(false);

                JLabel titleLabel = new JLabel(tabbedPane.getTitleAt(index));
                JButton closeButton = new JButton("X");
                closeButton.setMargin(new Insets(0, 5, 0, 5));
                closeButton.setBorderPainted(false);
                closeButton.setContentAreaFilled(false);

                closeButton.addActionListener(evt -> tabbedPane.removeTabAt(tabbedPane.indexOfTabComponent(tabHeader)));

                tabHeader.add(titleLabel);
                tabHeader.add(closeButton);

                tabbedPane.setTabComponentAt(index, tabHeader);
            }
        });
    }

    private void createFileTypeComboBox() {
        fileTypeComboBox = new JComboBox<>(fileTypes);
        fileTypeComboBox.setSelectedIndex(0);
        add(fileTypeComboBox, BorderLayout.SOUTH);
    }

    private void createFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private void createNewFile() {
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textPane);
        tabbedPane.addTab("Novo Arquivo", scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        String selectedFileType = (String) fileTypeComboBox.getSelectedItem();
        if (selectedFileType != null) {
            switch (selectedFileType) {
                case "HTML":
                    textPane.setContentType("text/html");
                    setupHtmlAutocomplete(textPane);
                    break;
                case "CSS":
                    textPane.setContentType("text/css");
                    break;
                case "JavaScript":
                    textPane.setContentType("text/javascript");
                    break;
                case "PHP":
                    textPane.setContentType("text/php");
                    break;
            }
        }
    }

    private void setupHtmlAutocomplete(JTextPane textPane) {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String text = textPane.getText();
                int caretPos = textPane.getCaretPosition();
                if (e.getKeyChar() == '<') {
                    String tag = getNextWord(text, caretPos);
                    if (tag.equals("")) {
                        return;
                    }
                    String[] htmlTags = {"html", "head", "body", "div", "p", "span", "a", "img", "h1", "h2", "h3", "ul", "li", "table", "tr", "td"};
                    for (String tagName : htmlTags) {
                        if (tagName.startsWith(tag)) {
                            textPane.replaceSelection("<" + tagName + ">");
                            break;
                        }
                    }
                }
            }
        });
    }

    private String getNextWord(String text, int caretPos) {
        int start = caretPos - 1;
        while (start > 0 && !Character.isWhitespace(text.charAt(start))) {
            start--;
        }
        return text.substring(start, caretPos);
    }

    private void saveCode() {
        try {
            int selectedTab = tabbedPane.getSelectedIndex();
            if (selectedTab != -1) {
                JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedTab);
                JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
                String code = textPane.getText();

                if (currentFile == null) {
                    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        currentFile = fileChooser.getSelectedFile();
                    }
                }

                if (currentFile != null) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                        writer.write(code);
                    }
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar o arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder code = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    code.append(line).append("\n");
                }

                JTextPane textPane = new JTextPane();
                textPane.setText(code.toString());
                JScrollPane scrollPane = new JScrollPane(textPane);
                tabbedPane.addTab(file.getName(), scrollPane);
                tabbedPane.setSelectedComponent(scrollPane);
                currentFile = file;

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir o arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void runCode() {
        // Lógica para executar o código
        JOptionPane.showMessageDialog(this, "Executando código...");
    }

    private void copyCode() {
        JTextPane textPane = getSelectedTextPane();
        if (textPane != null) {
            textPane.copy();
        }
    }

    private void pasteCode() {
        JTextPane textPane = getSelectedTextPane();
        if (textPane != null) {
            textPane.paste();
        }
    }

    private void clearCode() {
        JTextPane textPane = getSelectedTextPane();
        if (textPane != null) {
            textPane.setText("");
        }
    }

    private JTextPane getSelectedTextPane() {
        int selectedTab = tabbedPane.getSelectedIndex();
        if (selectedTab != -1) {
            JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(selectedTab);
            return (JTextPane) scrollPane.getViewport().getView();
        }
        return null;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        Color backgroundColor = isDarkMode ? Color.BLACK : Color.WHITE;
        Color textColor = isDarkMode ? Color.WHITE : Color.BLACK;

        getContentPane().setBackground(backgroundColor);
        menuBar.setBackground(backgroundColor);
        toolBar.setBackground(backgroundColor);

        UIManager.put("TextPane.background", backgroundColor);
        UIManager.put("TextPane.foreground", textColor);
        UIManager.put("MenuBar.background", backgroundColor);
        UIManager.put("MenuItem.background", backgroundColor);
        UIManager.put("MenuItem.foreground", textColor);
    }

    private void createHtmlTemplate() {
        createNewFile();
        JTextPane textPane = getSelectedTextPane();
        if (textPane != null) {
            String htmlTemplate = "<!DOCTYPE html>\n<html lang=\"pt-br\">\n<head>\n\t<meta charset=\"UTF-8\">\n\t<title>Título</title>\n</head>\n<body>\n</body>\n</html>";
            textPane.setText(htmlTemplate);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdvancedIDE().setVisible(true));
    }
}
