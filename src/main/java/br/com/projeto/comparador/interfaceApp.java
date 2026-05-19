package br.com.projeto.comparador;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.com.projeto.comparador.model.registroPlanilha;
import br.com.projeto.comparador.reader.leitorPlanilha;
import br.com.projeto.comparador.geradorRelatorioExcel;

public class interfaceApp extends Application {
    
    private TextField campoFinanceiro;
    private TextField campoCadastro;
    private TextArea areaResultado;
    private Button btnComparar;
    private ProgressIndicator progressIndicator;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Comparador de Planilhas - Estágios");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Topo (título)
        Label titulo = new Label("SISTEMA DE COMPARAÇÃO DE PLANILHAS");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.web("#2c3e50"));
        BorderPane.setAlignment(titulo, Pos.CENTER);
        root.setTop(titulo);
        
        // Centro (conteúdo principal)
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(20));
        
        // Seção: Planilha Financeiro
        VBox boxFinanceiro = criarBoxPlanilha("📊 Planilha do FINANCEIRO:", campoFinanceiro = new TextField());
        
        // Seção: Planilha Cadastro
        VBox boxCadastro = criarBoxPlanilha("📋 Planilha do CADASTRO:", campoCadastro = new TextField());
        
        // Botão Comparar
        btnComparar = new Button("🔍 COMPARAR PLANILHAS");
        btnComparar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnComparar.setPrefWidth(200);
        btnComparar.setOnAction(e -> compararPlanilhas());
        
        // Barra de progresso
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(40, 40);
        
        HBox buttonBox = new HBox(20, btnComparar, progressIndicator);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Área de resultado
        Label lblResultado = new Label("📄 RESULTADO DA COMPARAÇÃO");
        lblResultado.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblResultado.setTextFill(Color.web("#27ae60"));
        
        areaResultado = new TextArea();
        areaResultado.setEditable(false);
        areaResultado.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        areaResultado.setPrefHeight(300);
        
        VBox resultadoBox = new VBox(10, lblResultado, areaResultado);
        
        centerBox.getChildren().addAll(boxFinanceiro, boxCadastro, buttonBox, resultadoBox);
        root.setCenter(centerBox);
        
        // Rodapé
        Label footer = new Label("Desenvolvido para comparação de dados de estágios | CPF + Matrícula como chave");
        footer.setFont(Font.font("Arial", 10));
        footer.setTextFill(Color.web("#7f8c8d"));
        BorderPane.setAlignment(footer, Pos.CENTER);
        root.setBottom(footer);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox criarBoxPlanilha(String titulo, TextField campo) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-color: #f8f9fa; -fx-border-radius: 5;");
        
        Label label = new Label(titulo);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        HBox linha = new HBox(10);
        campo.setPromptText("Caminho da planilha...");
        campo.setPrefWidth(500);
        campo.setEditable(false);
        
        Button btnBuscar = new Button("📂 Buscar");
        btnBuscar.setOnAction(e -> buscarArquivo(campo));
        
        linha.getChildren().addAll(campo, btnBuscar);
        box.getChildren().addAll(label, linha);
        
        return box;
    }
    
    private void buscarArquivo(TextField campo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar planilha");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            campo.setText(file.getAbsolutePath());
        }
    }
    
    private void compararPlanilhas() {
        String caminhoFinanceiro = campoFinanceiro.getText();
        String caminhoCadastro = campoCadastro.getText();
        
        if (caminhoFinanceiro.isEmpty() || caminhoCadastro.isEmpty()) {
            areaResultado.setText("❌ ERRO: Selecione ambas as planilhas antes de comparar!");
            return;
        }
        
        // Desabilitar botão e mostrar progresso
        btnComparar.setDisable(true);
        progressIndicator.setVisible(true);
        areaResultado.setText("🔄 Lendo planilhas e comparando dados...\n");
        
        // Executar em thread separada para não travar a interface
        Thread thread = new Thread(() -> {
            try {
                // Ler planilhas
                List<registroPlanilha> financeiro = leitorPlanilha.ler(caminhoFinanceiro);
                List<registroPlanilha> cadastro = leitorPlanilha.ler(caminhoCadastro);
                
                // Comparar
                comparadorPlanilhas.ResultadoComparacao resultado = comparadorPlanilhas.comparar(financeiro, cadastro);

                // ===== DEBUG =====
                System.out.println("\n========== DEBUG ==========");
                System.out.println("Financeiro size: " + financeiro.size());
                System.out.println("Cadastro size: " + cadastro.size());
                System.out.println("Total Conformes: " + resultado.getTotalConformes());
                System.out.println("Total Faltantes: " + resultado.getTotalFaltantes());
                System.out.println("Total Excedentes: " + resultado.getTotalExcedentes());
                System.out.println("Total Divergencias: " + resultado.getTotalDivergencias());
                System.out.println("===========================\n");
                
                // Gerar texto do resultado
                String resultadoTexto = gerarTextoResultado(resultado, financeiro.size(), cadastro.size());
                
                // Atualizar interface (no thread do JavaFX)
                javafx.application.Platform.runLater(() -> {
                    areaResultado.setText(resultadoTexto);
                    btnComparar.setDisable(false);
                    progressIndicator.setVisible(false);
                    
                    // Perguntar se quer gerar Excel
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Relatório Excel");
                    alert.setHeaderText("Deseja gerar relatório Excel?");
                    alert.setContentText("O relatório Excel contém todas as divergências detalhadas.");
                    
                    if (alert.showAndWait().get() == ButtonType.OK) {
                        gerarRelatorioExcel(resultado, caminhoFinanceiro, caminhoCadastro, financeiro.size(), cadastro.size());
                    }
                });
                
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    areaResultado.setText("❌ ERRO ao ler as planilhas:\n" + e.getMessage());
                    btnComparar.setDisable(false);
                    progressIndicator.setVisible(false);
                });
            }
        });
        thread.start();
    }
    
    private String gerarTextoResultado(comparadorPlanilhas.ResultadoComparacao resultado, int totalFin, int totalCad) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=".repeat(70)).append("\n");
        sb.append("RESUMO DA COMPARAÇÃO\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append("🔑 Chave utilizada: CPF + Matrícula\n");
        sb.append(String.format("📊 Financeiro: %d registros | Cadastro: %d registros\n", totalFin, totalCad));
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("✅ Registros conformes: %d\n", resultado.getTotalConformes()));
        sb.append(String.format("❌ Faltantes no cadastro: %d\n", resultado.getTotalFaltantes()));
        sb.append(String.format("⚠️ Excedentes no cadastro: %d\n", resultado.getTotalExcedentes()));
        sb.append(String.format("🔄 Divergências: %d registros\n", resultado.getTotalDivergencias()));
        sb.append(String.format("   ├── ❌ Erros: %d\n", resultado.getTotalErros()));
        sb.append(String.format("   └── ⚠️ Avisos: %d\n", resultado.getTotalAvisos()));
        
        if (resultado.getTotalFaltantes() > 0) {
            sb.append("\n").append("-".repeat(70)).append("\n");
            sb.append("❌ FALTANTES NO CADASTRO:\n");
            for (registroPlanilha reg : resultado.getFaltantesNoCadastro()) {
                sb.append(String.format("  • %s | %s | %s\n", reg.getMatricula(), reg.getCpf(), reg.getNome()));
            }
        }
        
        if (resultado.getTotalExcedentes() > 0) {
            sb.append("\n").append("-".repeat(70)).append("\n");
            sb.append("⚠️ EXCEDENTES NO CADASTRO:\n");
            for (registroPlanilha reg : resultado.getExcedentesNoCadastro()) {
                sb.append(String.format("  • %s | %s | %s\n", reg.getMatricula(), reg.getCpf(), reg.getNome()));
            }
        }
        
        if (resultado.getTotalDivergencias() > 0) {
            sb.append("\n").append("-".repeat(70)).append("\n");
            sb.append("🔄 DIVERGÊNCIAS:\n");
            for (var entry : resultado.getDivergenciasPorChave().entrySet()) {
                String[] partes = entry.getKey().split("\\|");
                sb.append(String.format("\n  CPF: %s | Matrícula: %s\n", partes[0], partes[1]));
                for (var div : entry.getValue()) {
                    sb.append(div.toString()).append("\n");
                }
            }
        }
        
        sb.append("\n").append("=".repeat(70)).append("\n");
        return sb.toString();
    }
    
    private void gerarRelatorioExcel(comparadorPlanilhas.ResultadoComparacao resultado, String financeiro, String cadastro, int totalFinanceiro, int totalCadastro) {
        // Primeiro, pede o nome do arquivo
        TextInputDialog dialogNome = new TextInputDialog("relatorio.xlsx");
        dialogNome.setTitle("Nome do arquivo");
        dialogNome.setHeaderText("Digite o nome do arquivo");
        dialogNome.setContentText("Nome do arquivo Excel:");
        
        dialogNome.showAndWait().ifPresent(nome -> {
            if (!nome.endsWith(".xlsx")) {
                nome += ".xlsx";
            }
            
            // Depois, pede a pasta onde salvar
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Selecionar pasta para salvar o relatório");
            
            // Tenta abrir na pasta do usuário
            String userHome = System.getProperty("user.home");
            File defaultDir = new File(userHome + "\\Desktop");
            if (defaultDir.exists()) {
                directoryChooser.setInitialDirectory(defaultDir);
            }
            
            File pasta = directoryChooser.showDialog(null);
            
            if (pasta != null) {
                String caminhoCompleto = pasta.getAbsolutePath() + "\\" + nome;
                try {
                    // Passando os totais para o gerador
                    geradorRelatorioExcel.gerar(resultado, financeiro, cadastro, caminhoCompleto, totalFinanceiro, totalCadastro);
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText("Relatório gerado!");
                    alert.setContentText("Arquivo salvo em: " + caminhoCompleto);
                    alert.showAndWait();
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao gerar relatório");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cancelado");
                alert.setHeaderText("Nenhuma pasta selecionada");
                alert.setContentText("Relatório não foi gerado.");
                alert.showAndWait();
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}