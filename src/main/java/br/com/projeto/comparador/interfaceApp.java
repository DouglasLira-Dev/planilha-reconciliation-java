package br.com.projeto.comparador;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import br.com.projeto.comparador.model.registroPlanilha;
import br.com.projeto.comparador.reader.leitorPlanilha;
import br.com.projeto.comparador.geradorRelatorioExcel;
import br.com.projeto.comparador.service.AuthService;

public class interfaceApp extends Application {
    
    private TextField campoFinanceiro;
    private TextField campoCadastro;
    private TextArea areaResultado;
    private Button btnComparar;
    private ProgressIndicator progressIndicator;
    private String usuarioLogado;
    
    private ListView<String> listViewAbas;
    private Label lblAbas;
    private Tab tabGraficos;
    
    // Novos campos para gráficos
    private VBox graficosContainer;
    private PieChart pieChart;
    private BarChart<String, Number> barChart;
    private ToggleButton btnPizza;
    private ToggleButton btnBarras;
    
    @Override
    public void start(Stage primaryStage) {
        mostrarTelaLogin(primaryStage);
    }
    
    private void mostrarTelaLogin(Stage primaryStage) {
        Stage loginStage = new Stage();
        loginStage.setTitle("Login - Comparador de Planilhas");
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setResizable(false);
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        Label lblUsuario = new Label("Usuário:");
        TextField txtUsuario = new TextField();
        Label lblSenha = new Label("Senha:");
        PasswordField txtSenha = new PasswordField();
        Button btnLogin = new Button("Entrar");
        Label lblMensagem = new Label();
        lblMensagem.setTextFill(Color.RED);
        
        grid.add(lblUsuario, 0, 0);
        grid.add(txtUsuario, 1, 0);
        grid.add(lblSenha, 0, 1);
        grid.add(txtSenha, 1, 1);
        grid.add(btnLogin, 1, 2);
        grid.add(lblMensagem, 1, 3);
        
        btnLogin.setOnAction(e -> {
            String login = txtUsuario.getText().trim();
            String senha = txtSenha.getText();
            if (AuthService.autenticar(login, senha)) {
                usuarioLogado = login;
                loginStage.close();
                iniciarTelaPrincipal(primaryStage);
            } else {
                lblMensagem.setText("Usuário ou senha inválidos.");
                txtSenha.clear();
            }
        });
        
        Scene scene = new Scene(grid, 400, 250);
        loginStage.setScene(scene);
        loginStage.show();
    }
    
    private void iniciarTelaPrincipal(Stage primaryStage) {
        primaryStage.setTitle("Comparador de Planilhas - Estágios | Usuário: " + usuarioLogado);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(750);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        Label titulo = new Label("SISTEMA DE COMPARAÇÃO DE PLANILHAS");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.web("#2c3e50"));
        BorderPane.setAlignment(titulo, Pos.CENTER);
        root.setTop(titulo);
        
        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(20));
        
        // Financeiro
        VBox boxFinanceiro = criarBoxPlanilha("📊 Planilha do FINANCEIRO:", campoFinanceiro = new TextField());
        centerBox.getChildren().add(boxFinanceiro);
        
        // Cadastro (com botão personalizado)
        VBox boxCadastroCustom = new VBox(5);
        boxCadastroCustom.setPadding(new Insets(10));
        boxCadastroCustom.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-color: #f8f9fa; -fx-border-radius: 5;");
        Label labelCadastro = new Label("📋 Planilha do CADASTRO:");
        labelCadastro.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        HBox linhaCadastro = new HBox(10);
        campoCadastro = new TextField();
        campoCadastro.setPromptText("Caminho da planilha...");
        campoCadastro.setPrefWidth(500);
        campoCadastro.setEditable(false);
        Button btnBuscarCadastro = new Button("📂 Buscar");
        btnBuscarCadastro.setOnAction(e -> buscarArquivoCadastro());
        linhaCadastro.getChildren().addAll(campoCadastro, btnBuscarCadastro);
        boxCadastroCustom.getChildren().addAll(labelCadastro, linhaCadastro);
        centerBox.getChildren().add(boxCadastroCustom);
        
        // Seleção de abas
        lblAbas = new Label("📑 Abas do cadastro (selecione uma ou mais):");
        lblAbas.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        listViewAbas = new ListView<>();
        listViewAbas.setPrefHeight(120);
        listViewAbas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewAbas.setPlaceholder(new Label("Selecione a planilha de cadastro primeiro"));
        listViewAbas.setDisable(true);
        lblAbas.setDisable(true);
        VBox boxAbas = new VBox(5, lblAbas, listViewAbas);
        boxAbas.setPadding(new Insets(10));
        centerBox.getChildren().add(boxAbas);
        
        // Botão Comparar
        btnComparar = new Button("🔍 COMPARAR PLANILHAS");
        btnComparar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnComparar.setPrefWidth(200);
        btnComparar.setOnAction(e -> compararPlanilhas());
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(40, 40);
        
        HBox buttonBox = new HBox(20, btnComparar, progressIndicator);
        buttonBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().add(buttonBox);
        
        // Abas de resultado (textual + gráfico)
        TabPane tabPaneResultados = new TabPane();
        tabPaneResultados.setPrefHeight(350);
        
        Tab tabTexto = new Tab("📄 Resumo Textual");
        areaResultado = new TextArea();
        areaResultado.setEditable(false);
        areaResultado.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        areaResultado.setPrefHeight(300);
        tabTexto.setContent(areaResultado);
        
        // --- Aba de Gráficos (modificada) ---
        tabGraficos = new Tab("📊 Gráficos");
        graficosContainer = new VBox(10);
        graficosContainer.setAlignment(Pos.CENTER);
        graficosContainer.setPadding(new Insets(10));
        
        // Botões de alternância
        ToggleGroup group = new ToggleGroup();
        btnPizza = new ToggleButton("🍕 Gráfico de Pizza");
        btnBarras = new ToggleButton("📊 Gráfico de Barras");
        btnPizza.setToggleGroup(group);
        btnBarras.setToggleGroup(group);
        btnPizza.setSelected(true);
        
        HBox toggleBox = new HBox(10, btnPizza, btnBarras);
        toggleBox.setAlignment(Pos.CENTER);
        
        Button btnExportar = new Button("📸 Exportar Gráfico como PNG");
        btnExportar.setOnAction(e -> exportarGraficoAtual());
        
        graficosContainer.getChildren().addAll(toggleBox, btnExportar);
        tabGraficos.setContent(graficosContainer);
        // --------------------------------
        
        tabPaneResultados.getTabs().addAll(tabTexto, tabGraficos);
        centerBox.getChildren().add(tabPaneResultados);
        
        root.setCenter(centerBox);
        
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
        btnBuscar.setOnAction(e -> buscarArquivoFinanceiro(campo));
        
        linha.getChildren().addAll(campo, btnBuscar);
        box.getChildren().addAll(label, linha);
        
        return box;
    }
    
    private void buscarArquivoFinanceiro(TextField campo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar planilha do financeiro");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            campo.setText(file.getAbsolutePath());
        }
    }
    
    private void buscarArquivoCadastro() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar planilha de cadastro");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Arquivos Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            campoCadastro.setText(file.getAbsolutePath());
            try {
                List<String> abas = leitorPlanilha.listarNomesAbasDados(file.getAbsolutePath());
                listViewAbas.getItems().setAll(abas);
                if (!abas.isEmpty()) {
                    listViewAbas.getSelectionModel().selectAll();
                    listViewAbas.setDisable(false);
                    lblAbas.setDisable(false);
                } else {
                    listViewAbas.setPlaceholder(new Label("Nenhuma aba de dados encontrada"));
                    listViewAbas.setDisable(true);
                    lblAbas.setDisable(true);
                }
            } catch (IOException e) {
                areaResultado.setText("❌ Erro ao ler as abas: " + e.getMessage());
            }
        }
    }
    
    private void compararPlanilhas() {
        String caminhoFinanceiro = campoFinanceiro.getText();
        String caminhoCadastro = campoCadastro.getText();

        if (caminhoFinanceiro.isEmpty() || caminhoCadastro.isEmpty()) {
            areaResultado.setText("❌ ERRO: Selecione ambas as planilhas antes de comparar!");
            return;
        }

        btnComparar.setDisable(true);
        progressIndicator.setVisible(true);
        areaResultado.setText("🔄 Lendo planilhas e comparando dados...\n");

        Thread thread = new Thread(() -> {
            try {
                // 1. Ler financeiro
                List<registroPlanilha> financeiroOriginal = leitorPlanilha.ler(caminhoFinanceiro);
                List<registroPlanilha> financeiroFiltrado = new ArrayList<>(financeiroOriginal);

                // 2. Filtro por intervalo (data mínima e máxima)
                final boolean[] aplicarFiltro = {false};
                final LocalDate[] dataMin = {null};
                final LocalDate[] dataMax = {null};
                final CountDownLatch latch = new CountDownLatch(1);

                javafx.application.Platform.runLater(() -> {
                    Alert alertFiltro = new Alert(Alert.AlertType.CONFIRMATION);
                    alertFiltro.setTitle("Filtrar por período");
                    alertFiltro.setHeaderText("Deseja filtrar os registros do financeiro por um intervalo de datas de início?");
                    alertFiltro.setContentText("Escolha 'Sim' para informar o período.");
                    ButtonType btnSim = new ButtonType("Sim");
                    ButtonType btnNao = new ButtonType("Não");
                    alertFiltro.getButtonTypes().setAll(btnSim, btnNao);

                    Optional<ButtonType> resultFiltro = alertFiltro.showAndWait();
                    if (resultFiltro.isPresent() && resultFiltro.get() == btnSim) {
                        try {
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
                            
                            // Data mínima (obrigatória)
                            TextInputDialog inputMesMin = new TextInputDialog();
                            inputMesMin.setTitle("Data de início (mínima)");
                            inputMesMin.setHeaderText("Informe o mês/ano de início (mínimo)");
                            inputMesMin.setContentText("Formato: MM/yyyy (ex: 05/2025)");
                            Optional<String> resultMin = inputMesMin.showAndWait();
                            if (resultMin.isPresent()) {
                                YearMonth yearMonthMin = YearMonth.parse(resultMin.get(), fmt);
                                dataMin[0] = yearMonthMin.atDay(1);
                                aplicarFiltro[0] = true;
                            }

                            // Data máxima (opcional)
                            if (aplicarFiltro[0]) {
                                Alert alertMax = new Alert(Alert.AlertType.CONFIRMATION);
                                alertMax.setTitle("Data máxima");
                                alertMax.setHeaderText("Deseja definir também uma data de início máxima?");
                                alertMax.setContentText("Se não, será considerado apenas o limite mínimo.");
                                ButtonType btnSimMax = new ButtonType("Sim");
                                ButtonType btnNaoMax = new ButtonType("Não");
                                alertMax.getButtonTypes().setAll(btnSimMax, btnNaoMax);
                                Optional<ButtonType> resultMax = alertMax.showAndWait();
                                if (resultMax.isPresent() && resultMax.get() == btnSimMax) {
                                    TextInputDialog inputMesMax = new TextInputDialog();
                                    inputMesMax.setTitle("Data de início (máxima)");
                                    inputMesMax.setHeaderText("Informe o mês/ano de início (máximo)");
                                    inputMesMax.setContentText("Formato: MM/yyyy (ex: 12/2025)");
                                    Optional<String> resultMesMax = inputMesMax.showAndWait();
                                    if (resultMesMax.isPresent()) {
                                        YearMonth yearMonthMax = YearMonth.parse(resultMesMax.get(), fmt);
                                        dataMax[0] = yearMonthMax.atEndOfMonth();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Formato inválido. Nenhum filtro aplicado.");
                        }
                    }
                    latch.countDown();
                });

                latch.await();

                if (aplicarFiltro[0] && dataMin[0] != null) {
                    int totalAntes = financeiroFiltrado.size();
                    financeiroFiltrado = filtrarPorIntervalo(financeiroFiltrado, dataMin[0], dataMax[0]);
                    String msg = "Financeiro filtrado: " + financeiroFiltrado.size() + " registros (início >= " + dataMin[0];
                    if (dataMax[0] != null) {
                        msg += " e <= " + dataMax[0];
                    }
                    msg += "). " + (totalAntes - financeiroFiltrado.size()) + " registros descartados.";
                    System.out.println(msg);
                    final String finalMsg = msg;
                    javafx.application.Platform.runLater(() -> {
                        areaResultado.setText("📅 Filtro aplicado: " + finalMsg + "\n" + areaResultado.getText());
                    });
                }

                // 3. Ler cadastro usando as abas selecionadas
                List<registroPlanilha> cadastro;
                List<String> abasSelecionadas = listViewAbas.getSelectionModel().getSelectedItems();
                if (abasSelecionadas == null || abasSelecionadas.isEmpty()) {
                    cadastro = leitorPlanilha.ler(caminhoCadastro);
                } else {
                    cadastro = leitorPlanilha.ler(caminhoCadastro, abasSelecionadas);
                }

                // 4. Comparar
                comparadorPlanilhas.ResultadoComparacao resultado = comparadorPlanilhas.comparar(financeiroFiltrado, cadastro);

                // Debug
                System.out.println("\n========== DEBUG ==========");
                System.out.println("Financeiro size (após filtro): " + financeiroFiltrado.size());
                System.out.println("Cadastro size: " + cadastro.size());
                System.out.println("Total Conformes: " + resultado.getTotalConformes());
                System.out.println("Total Faltantes: " + resultado.getTotalFaltantes());
                System.out.println("Total Excedentes: " + resultado.getTotalExcedentes());
                System.out.println("Total Divergencias: " + resultado.getTotalDivergencias());
                System.out.println("===========================\n");

                int totalFin = financeiroFiltrado.size();
                int totalCad = cadastro.size();
                String resultadoTexto = gerarTextoResultado(resultado, totalFin, totalCad);

                // Atualizar interface
                javafx.application.Platform.runLater(() -> {
                    areaResultado.setText(resultadoTexto);
                    criarGraficos(resultado);   // cria ambos os gráficos e configura alternância
                    btnComparar.setDisable(false);
                    progressIndicator.setVisible(false);

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Relatório Excel");
                    alert.setHeaderText("Deseja gerar relatório Excel?");
                    alert.setContentText("O relatório Excel contém todas as divergências detalhadas.");

                    if (alert.showAndWait().get() == ButtonType.OK) {
                        gerarRelatorioExcel(resultado, caminhoFinanceiro, caminhoCadastro, totalFin, totalCad);
                    }
                });

            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    areaResultado.setText("❌ ERRO ao ler as planilhas:\n" + e.getMessage());
                    btnComparar.setDisable(false);
                    progressIndicator.setVisible(false);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
    }
    
    private List<registroPlanilha> filtrarPorIntervalo(List<registroPlanilha> registros, LocalDate dataMin, LocalDate dataMax) {
        List<registroPlanilha> filtrados = new ArrayList<>();
        for (registroPlanilha reg : registros) {
            LocalDate inicio = reg.getDataInicio();
            if (inicio == null) continue;
            if (inicio.isBefore(dataMin)) continue;
            if (dataMax != null && inicio.isAfter(dataMax)) continue;
            filtrados.add(reg);
        }
        return filtrados;
    }
    
    // ========== NOVOS MÉTODOS PARA GRÁFICOS ==========
    private void criarGraficos(comparadorPlanilhas.ResultadoComparacao resultado) {
        // Gráfico de Pizza
        pieChart = new PieChart();
        pieChart.setTitle("Distribuição da Comparação");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        adicionarDadosPizza(resultado);
        
        // Gráfico de Barras
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Categoria");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantidade");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Comparação por Categoria");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(400);
        adicionarDadosBarras(resultado);
        
        // Alternância entre os gráficos
        btnPizza.setOnAction(e -> mostrarGrafico(pieChart));
        btnBarras.setOnAction(e -> mostrarGrafico(barChart));
        
        // Mostra o pizza por padrão
        mostrarGrafico(pieChart);
    }
    
    // *** ÚNICA PARTE MODIFICADA (forçar label da fatia Excedentes) ***
    private void adicionarDadosPizza(comparadorPlanilhas.ResultadoComparacao resultado) {
        pieChart.getData().clear();
        
        int conforme = resultado.getTotalConformes();
        int faltante = resultado.getTotalFaltantes();
        int excedente = resultado.getTotalExcedentes();
        int divergente = resultado.getTotalDivergencias();
        int cancelado = resultado.getTotalCancelados();
        int total = conforme + faltante + excedente + divergente + cancelado;
        
        if (total == 0) return;
        
        java.util.function.Function<Integer, String> formatar = (valor) -> {
            double percent = (valor * 100.0) / total;
            return String.format(" (%.1f%%)", percent);
        };
        
        if (conforme > 0)
            pieChart.getData().add(new PieChart.Data("Conformes" + formatar.apply(conforme), conforme));
        if (faltante > 0)
            pieChart.getData().add(new PieChart.Data("Faltantes" + formatar.apply(faltante), faltante));
        if (excedente > 0)
            pieChart.getData().add(new PieChart.Data("Excedentes" + formatar.apply(excedente), excedente));
        if (divergente > 0)
            pieChart.getData().add(new PieChart.Data("Divergências" + formatar.apply(divergente), divergente));
        if (cancelado > 0)
            pieChart.getData().add(new PieChart.Data("Cancelados" + formatar.apply(cancelado), cancelado));
        
        // Configurações agressivas para tentar exibir a label mesmo em fatias pequenas
        pieChart.setLabelLineLength(30);          // linha longa
        pieChart.setLabelsVisible(true);
        pieChart.setStartAngle(90);
        pieChart.setClockwise(true);
        pieChart.setPrefSize(700, 700);           // área bem grande
        
        // CSS para forçar visibilidade
        pieChart.setStyle(".chart-pie-label { -fx-font-size: 11px; } .chart-pie-label-line { -fx-stroke-width: 2; }");
        
        // Força a label de cada fatia (incluindo a Excedentes)
        pieChart.getData().forEach(data -> {
            data.getNode().setStyle("-fx-pie-label-visible: true;");
            // Tenta acessar o nó da label e torná-lo visível (se possível)
            data.getNode().applyCss();
            data.getNode().setVisible(true);
        });
        
        // Pequena pausa para permitir o layout (não é garantido, mas ajuda)
        try { Thread.sleep(50); } catch (InterruptedException e) { }
    }
    // ********************************************
    
    private void adicionarDadosBarras(comparadorPlanilhas.ResultadoComparacao resultado) {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantidade");
        series.getData().add(new XYChart.Data<>("Conformes", resultado.getTotalConformes()));
        series.getData().add(new XYChart.Data<>("Faltantes", resultado.getTotalFaltantes()));
        series.getData().add(new XYChart.Data<>("Excedentes", resultado.getTotalExcedentes()));
        series.getData().add(new XYChart.Data<>("Divergências", resultado.getTotalDivergencias()));
        series.getData().add(new XYChart.Data<>("Cancelados", resultado.getTotalCancelados()));
        barChart.getData().add(series);
        
        barChart.lookupAll(".default-color0.chart-bar").forEach(node -> node.setStyle("-fx-bar-fill: #2ecc71;"));
        barChart.lookupAll(".default-color1.chart-bar").forEach(node -> node.setStyle("-fx-bar-fill: #e74c3c;"));
        barChart.lookupAll(".default-color2.chart-bar").forEach(node -> node.setStyle("-fx-bar-fill: #f39c12;"));
        barChart.lookupAll(".default-color3.chart-bar").forEach(node -> node.setStyle("-fx-bar-fill: #9b59b6;"));
        barChart.lookupAll(".default-color4.chart-bar").forEach(node -> node.setStyle("-fx-bar-fill: #95a5a6;"));
    }
    
    private void mostrarGrafico(Node graph) {
        graficosContainer.getChildren().removeIf(node -> node instanceof PieChart || node instanceof BarChart);
        graficosContainer.getChildren().add(1, graph);
    }
    
    private void exportarGraficoAtual() {
        Node currentGraph = graficosContainer.getChildren().stream()
                .filter(node -> node instanceof PieChart || node instanceof BarChart)
                .findFirst()
                .orElse(null);
        if (currentGraph == null) {
            new Alert(Alert.AlertType.WARNING, "Nenhum gráfico disponível para exportar.").show();
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar gráfico como PNG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                WritableImage image = currentGraph.snapshot(null,null);
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".png")) path += ".png";
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(path));
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText("Gráfico exportado!");
                alert.setContentText("Arquivo salvo em: " + path);
                alert.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erro ao exportar gráfico: " + ex.getMessage()).show();
            }
        }
    }
    // ============================================
    
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
        TextInputDialog dialogNome = new TextInputDialog("relatorio.xlsx");
        dialogNome.setTitle("Nome do arquivo");
        dialogNome.setHeaderText("Digite o nome do arquivo");
        dialogNome.setContentText("Nome do arquivo Excel:");
        
        dialogNome.showAndWait().ifPresent(nome -> {
            if (!nome.endsWith(".xlsx")) {
                nome += ".xlsx";
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Selecionar pasta para salvar o relatório");
            String userHome = System.getProperty("user.home");
            File defaultDir = new File(userHome + "\\Desktop");
            if (defaultDir.exists()) {
                directoryChooser.setInitialDirectory(defaultDir);
            }
            
            File pasta = directoryChooser.showDialog(null);
            if (pasta != null) {
                String caminhoCompleto = pasta.getAbsolutePath() + "\\" + nome;
                try {
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