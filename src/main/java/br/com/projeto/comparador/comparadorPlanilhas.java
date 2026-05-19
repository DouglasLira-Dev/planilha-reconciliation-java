package br.com.projeto.comparador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.projeto.comparador.model.registroPlanilha;
import br.com.projeto.comparador.util.normalizacao;

public class comparadorPlanilhas {
    
    // Configurações de comparação
    public static class ConfiguracaoComparacao {
        private double limiarSimilaridadeNomes = 85.0; // 85% de similaridade mínima
        private boolean compararMatricula = true;
        private boolean compararCPF = true;
        private boolean compararNome = true;
        private boolean compararNivelEstagio = true;
        private boolean compararDataInicio = true;
        private boolean compararDataFim = true;
        private boolean compararBanco = true;
        private boolean compararAgencia = true;
        private boolean compararConta = true;
        
        // Getters e Setters
        public double getLimiarSimilaridadeNomes() { return limiarSimilaridadeNomes; }
        public void setLimiarSimilaridadeNomes(double limiar) { this.limiarSimilaridadeNomes = limiar; }
        
        public boolean isCompararMatricula() { return compararMatricula; }
        public void setCompararMatricula(boolean compararMatricula) { this.compararMatricula = compararMatricula; }
        
        public boolean isCompararCPF() { return compararCPF; }
        public void setCompararCPF(boolean compararCPF) { this.compararCPF = compararCPF; }
        
        public boolean isCompararNome() { return compararNome; }
        public void setCompararNome(boolean compararNome) { this.compararNome = compararNome; }
        
        public boolean isCompararNivelEstagio() { return compararNivelEstagio; }
        public void setCompararNivelEstagio(boolean compararNivelEstagio) { this.compararNivelEstagio = compararNivelEstagio; }
        
        public boolean isCompararDataInicio() { return compararDataInicio; }
        public void setCompararDataInicio(boolean compararDataInicio) { this.compararDataInicio = compararDataInicio; }
        
        public boolean isCompararDataFim() { return compararDataFim; }
        public void setCompararDataFim(boolean compararDataFim) { this.compararDataFim = compararDataFim; }
        
        public boolean isCompararBanco() { return compararBanco; }
        public void setCompararBanco(boolean compararBanco) { this.compararBanco = compararBanco; }
        
        public boolean isCompararAgencia() { return compararAgencia; }
        public void setCompararAgencia(boolean compararAgencia) { this.compararAgencia = compararAgencia; }
        
        public boolean isCompararConta() { return compararConta; }
        public void setCompararConta(boolean compararConta) { this.compararConta = compararConta; }
        
        // Configuração padrão
        public static ConfiguracaoComparacao criarPadrao() {
            return new ConfiguracaoComparacao();
        }
        
        // Configuração para comparar apenas campos essenciais
        public static ConfiguracaoComparacao criarEssencial() {
            ConfiguracaoComparacao config = new ConfiguracaoComparacao();
            config.setCompararBanco(false);
            config.setCompararAgencia(false);
            config.setCompararConta(false);
            return config;
        }
    }
    
    // Classe para armazenar divergências
    public static class Divergencia {
        private String chave;
        private String campo;
        private String valorFinanceiro;
        private String valorCadastro;
        private String tipo; // "ERRO" ou "AVISO"
        private Double similaridade; // Para nomes similares
        
        public Divergencia(String chave, String campo, String valorFinanceiro, String valorCadastro) {
            this(chave, campo, valorFinanceiro, valorCadastro, "ERRO", null);
        }
        
        public Divergencia(String chave, String campo, String valorFinanceiro, String valorCadastro, String tipo, Double similaridade) {
            this.chave = chave;
            this.campo = campo;
            this.valorFinanceiro = valorFinanceiro;
            this.valorCadastro = valorCadastro;
            this.tipo = tipo;
            this.similaridade = similaridade;
        }
        
        public String getChave() { return chave; }
        public String getCampo() { return campo; }
        public String getValorFinanceiro() { return valorFinanceiro; }
        public String getValorCadastro() { return valorCadastro; }
        public String getTipo() { return tipo; }
        public Double getSimilaridade() { return similaridade; }
        
        @Override
        public String toString() {
            if (tipo.equals("AVISO")) {
                return String.format("  ⚠️ %s: [Financeiro: %s] vs [Cadastro: %s] (similaridade: %.1f%%)", 
                    campo, valorFinanceiro, valorCadastro, similaridade);
            } else {
                return String.format("  ❌ %s: [Financeiro: %s] vs [Cadastro: %s]", 
                    campo, valorFinanceiro, valorCadastro);
            }
        }
    }
    
    // Classe para armazenar o resultado completo
    public static class ResultadoComparacao {
        private List<registroPlanilha> faltantesNoCadastro;
        private List<registroPlanilha> excedentesNoCadastro;
        private Map<String, List<Divergencia>> divergenciasPorChave;
        private List<registroPlanilha> conflitosCPFMatricula;
        private int totalConformes;
        private ConfiguracaoComparacao configuracao;
        private List<registroPlanilha> canceladosNoCadastro;
        
        public ResultadoComparacao() {
            this.faltantesNoCadastro = new ArrayList<>();
            this.excedentesNoCadastro = new ArrayList<>();
            this.divergenciasPorChave = new HashMap<>();
            this.conflitosCPFMatricula = new ArrayList<>();
            this.totalConformes = 0;
            this.configuracao = ConfiguracaoComparacao.criarPadrao();
            this.canceladosNoCadastro = new ArrayList<>();
        }
        
        // Getters
        public List<registroPlanilha> getFaltantesNoCadastro() { return faltantesNoCadastro; }
        public List<registroPlanilha> getExcedentesNoCadastro() { return excedentesNoCadastro; }
        public List<registroPlanilha> getCanceladosNoCadastro() { return canceladosNoCadastro; }
        public Map<String, List<Divergencia>> getDivergenciasPorChave() { return divergenciasPorChave; }
        public List<registroPlanilha> getConflitosCPFMatricula() { return conflitosCPFMatricula; }
        public int getTotalConformes() { return totalConformes; }
        public ConfiguracaoComparacao getConfiguracao() { return configuracao; }
        
        public void setTotalConformes(int totalConformes) { this.totalConformes = totalConformes; }
        public void setConfiguracao(ConfiguracaoComparacao configuracao) { this.configuracao = configuracao; }
        
        public int getTotalFaltantes() { return faltantesNoCadastro.size(); }
        public int getTotalExcedentes() { return excedentesNoCadastro.size(); }
        public int getTotalCancelados() { return canceladosNoCadastro.size(); }
        public int getTotalDivergencias() { return divergenciasPorChave.size(); }
        public int getTotalConflitos() { return conflitosCPFMatricula.size(); }
        public int getTotalErros() { 
            int total = 0;
            for (List<Divergencia> lista : divergenciasPorChave.values()) {
                for (Divergencia d : lista) {
                    if (d.getTipo().equals("ERRO")) total++;
                }
            }
            return total;
        }
        public int getTotalAvisos() {
            int total = 0;
            for (List<Divergencia> lista : divergenciasPorChave.values()) {
                for (Divergencia d : lista) {
                    if (d.getTipo().equals("AVISO")) total++;
                }
            }
            return total;
        }
        
        public void addFaltante(registroPlanilha reg) { faltantesNoCadastro.add(reg); }
        public void addExcedente(registroPlanilha reg) { excedentesNoCadastro.add(reg); }
        public void addCancelado(registroPlanilha reg) { this.canceladosNoCadastro.add(reg); }
        public void addConflito(registroPlanilha reg) { conflitosCPFMatricula.add(reg); }
        
        public void addDivergencia(String chave, Divergencia div) {
            divergenciasPorChave.computeIfAbsent(chave, k -> new ArrayList<>()).add(div);
        }
    }
    
    /**
     * Gera uma chave composta por CPF + Matrícula
     */
    private static String chaveComposta(registroPlanilha reg) {
        return reg.getCpfNorm() + "|" + reg.getMatriculaNorm();
    }
    
    /**
     * Compara dois registros e retorna lista de divergências
     */
    private static List<Divergencia> compararRegistros(String chave, registroPlanilha regFin, registroPlanilha regCad, ConfiguracaoComparacao config) {
        List<Divergencia> divergencias = new ArrayList<>();
        
        // Comparar Matrícula (sempre comparar, é parte da chave)
        if (!regFin.getMatriculaNorm().equals(regCad.getMatriculaNorm())) {
            divergencias.add(new Divergencia(chave, "Matrícula", regFin.getMatriculaNorm(), regCad.getMatriculaNorm()));
        }
        
        // Comparar CPF (sempre comparar, é parte da chave)
        if (!regFin.getCpfNorm().equals(regCad.getCpfNorm())) {
            divergencias.add(new Divergencia(chave, "CPF", regFin.getCpfNorm(), regCad.getCpfNorm()));
        }
        
        // Comparar Nome com similaridade fuzzy
        if (config.isCompararNome()) {
            if (!regFin.getNomeNorm().equals(regCad.getNomeNorm())) {
                double similaridadeNome = normalizacao.similaridade(regFin.getNomeNorm(), regCad.getNomeNorm());
                
                // Verificar se é erro de digitação (diferença de 1-2 caracteres)
                if (normalizacao.isErroDigitacao(regFin.getNomeNorm(), regCad.getNomeNorm())) {
                    // Erro de digitação = ERRO
                    divergencias.add(new Divergencia(chave, "Nome", 
                        regFin.getNomeNorm(), regCad.getNomeNorm(), "ERRO", null));
                }
                // Verificar se é abreviação
                else if (normalizacao.isAbreviacao(regFin.getNomeNorm(), regCad.getNomeNorm())) {
                    // Abreviação = AVISO
                    divergencias.add(new Divergencia(chave, "Nome (abreviação)", 
                        regFin.getNomeNorm(), regCad.getNomeNorm(), "AVISO", similaridadeNome));
                }
                // Se for similar mas não é erro de digitação nem abreviação
                else if (similaridadeNome >= config.getLimiarSimilaridadeNomes()) {
                    // Similar = AVISO
                    divergencias.add(new Divergencia(chave, "Nome (similar)", 
                        regFin.getNomeNorm(), regCad.getNomeNorm(), "AVISO", similaridadeNome));
                } else {
                    // Diferente = ERRO
                    divergencias.add(new Divergencia(chave, "Nome", 
                        regFin.getNomeNorm(), regCad.getNomeNorm(), "ERRO", null));
                }
            }
        }
        
        // Comparar Nível Estágio
        if (config.isCompararNivelEstagio()) {
            if (!regFin.getNivelEstagioNorm().equals(regCad.getNivelEstagioNorm())) {
                divergencias.add(new Divergencia(chave, "Nível Estágio", regFin.getNivelEstagioNorm(), regCad.getNivelEstagioNorm()));
            }
        }
        
        // Comparar Data Início
        if (config.isCompararDataInicio()) {
            if (!Objects.equals(regFin.getDataInicio(), regCad.getDataInicio())) {
                divergencias.add(new Divergencia(chave, "Data Início", 
                    regFin.getDataInicio() != null ? regFin.getDataInicio().toString() : "null",
                    regCad.getDataInicio() != null ? regCad.getDataInicio().toString() : "null"));
            }
        }
        
        // Comparar Data Fim
        if (config.isCompararDataFim()) {
            if (!Objects.equals(regFin.getDataFim(), regCad.getDataFim())) {
                divergencias.add(new Divergencia(chave, "Data Fim",
                    regFin.getDataFim() != null ? regFin.getDataFim().toString() : "null",
                    regCad.getDataFim() != null ? regCad.getDataFim().toString() : "null"));
            }
        }
        
        // Comparar Banco
        if (config.isCompararBanco()) {
            if (!regFin.getBancoNorm().equals(regCad.getBancoNorm())) {
                divergencias.add(new Divergencia(chave, "Banco", regFin.getBancoNorm(), regCad.getBancoNorm()));
            }
        }
        
        // Comparar Agência
        if (config.isCompararAgencia()) {
            if (!regFin.getAgenciaNorm().equals(regCad.getAgenciaNorm())) {
                divergencias.add(new Divergencia(chave, "Agência", regFin.getAgenciaNorm(), regCad.getAgenciaNorm()));
            }
        }
        
        // Comparar Conta
        if (config.isCompararConta()) {
            if (!regFin.getContaNorm().equals(regCad.getContaNorm())) {
                divergencias.add(new Divergencia(chave, "Conta", regFin.getContaNorm(), regCad.getContaNorm()));
            }
        }
        
        return divergencias;
    }
    
    /**
     * Compara duas listas de registros (financeiro vs cadastro)
     * A chave de identificação é a combinação CPF + Matrícula
     * 
     * @param financeiro Lista da planilha do financeiro
     * @param cadastro Lista da planilha de cadastro
     * @return Resultado da comparação
     */
    public static ResultadoComparacao comparar(List<registroPlanilha> financeiro, 
                                                List<registroPlanilha> cadastro) {
        return comparar(financeiro, cadastro, ConfiguracaoComparacao.criarPadrao());
    }
    
    /**
     * Compara duas listas de registros com configuração personalizada
     * 
     * @param financeiro Lista da planilha do financeiro
     * @param cadastro Lista da planilha de cadastro
     * @param config Configuração da comparação
     * @return Resultado da comparação
     */
    public static ResultadoComparacao comparar(List<registroPlanilha> financeiro, 
                                                List<registroPlanilha> cadastro,
                                                ConfiguracaoComparacao config
                                            ) {
        ResultadoComparacao resultado = new ResultadoComparacao();
        resultado.setConfiguracao(config);
        
        // Criar mapas usando chave composta (CPF|Matrícula)
        Map<String, registroPlanilha> mapFinanceiro = new HashMap<>();
        Map<String, registroPlanilha> mapCadastro = new HashMap<>();
        Map<String, registroPlanilha> mapPorCPF = new HashMap<>();
        
        for (registroPlanilha reg : financeiro) {
            String chave = chaveComposta(reg);
            if (chave != null && !chave.isEmpty() && !chave.equals("|")) {
                mapFinanceiro.put(chave, reg);
                mapPorCPF.put(reg.getCpfNorm(), reg);
            }
        }
        
        for (registroPlanilha reg : cadastro) {
            if (reg.isCancelado()) {
                 resultado.addCancelado(reg);
                continue;
                }
            String chave = chaveComposta(reg);
            if (chave != null && !chave.isEmpty() && !chave.equals("|")) {
                mapCadastro.put(chave, reg);
            }
        }
        
        // 1. Verificar faltantes (está no financeiro, não está no cadastro)
        for (Map.Entry<String, registroPlanilha> entry : mapFinanceiro.entrySet()) {
            String chave = entry.getKey();
            if (!mapCadastro.containsKey(chave)) {
                resultado.addFaltante(entry.getValue());
            }
        }
        
        // 2. Verificar excedentes (está no cadastro, não está no financeiro)
        for (Map.Entry<String, registroPlanilha> entry : mapCadastro.entrySet()) {
            String chave = entry.getKey();
            if (!mapFinanceiro.containsKey(chave)) {
                resultado.addExcedente(entry.getValue());
            }
        }
        
        // 3. Verificar divergências (está nas duas, com mesmo CPF+Matrícula)
        for (Map.Entry<String, registroPlanilha> entry : mapFinanceiro.entrySet()) {
            String chave = entry.getKey();
            if (mapCadastro.containsKey(chave)) {
                registroPlanilha regFin = entry.getValue();
                registroPlanilha regCad = mapCadastro.get(chave);
                
                List<Divergencia> divergencias = compararRegistros(chave, regFin, regCad, config);
                
                if (!divergencias.isEmpty()) {
                    for (Divergencia div : divergencias) {
                        resultado.addDivergencia(chave, div);
                    }
                } else {
                    resultado.setTotalConformes(resultado.getTotalConformes() + 1);
                }
            }
        }
        
        // 4. Detectar conflitos: mesmo CPF mas matrícula diferente em cada sistema
        for (registroPlanilha regCad : cadastro) {
            String cpf = regCad.getCpfNorm();
            if (mapPorCPF.containsKey(cpf)) {
                registroPlanilha regFin = mapPorCPF.get(cpf);
                if (!regFin.getMatriculaNorm().equals(regCad.getMatriculaNorm())) {
                    String chaveConflito = cpf + "|" + regCad.getMatriculaNorm();
                    if (!mapFinanceiro.containsKey(chaveConflito) && !mapCadastro.containsKey(chaveComposta(regFin))) {
                        resultado.addConflito(regCad);
                    }
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Exibe o resultado da comparação no console
     */
    public static void exibirResultado(ResultadoComparacao resultado) {
        ConfiguracaoComparacao config = resultado.getConfiguracao();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RESUMO DA COMPARAÇÃO");
        System.out.println("=".repeat(70));
        System.out.println("🔑 Chave utilizada: CPF + Matrícula (combinação)");
        System.out.println("🎯 Limiar similaridade de nomes: " + config.getLimiarSimilaridadeNomes() + "%");
        System.out.println("-".repeat(70));
        System.out.println("⚠️ Cancelados no cadastro: " + resultado.getTotalCancelados());
        System.out.println("✅ Registros conformes (idênticos): " + resultado.getTotalConformes());
        System.out.println("❌ Registros que se encontram apenas no financeiro: " + resultado.getTotalFaltantes());
        System.out.println("⚠️  Registros que não estão no financeiro: " + resultado.getTotalExcedentes());
        System.out.println("🔄 Divergências: " + resultado.getTotalDivergencias() + " registros");
        System.out.println("   ├── ❌ Erros: " + resultado.getTotalErros());
        System.out.println("   └── ⚠️  Avisos: " + resultado.getTotalAvisos());
        System.out.println("⚡ Conflitos (mesmo CPF, matrícula diferente): " + resultado.getTotalConflitos());
        
        if (resultado.getTotalFaltantes() > 0) {
            System.out.println("\n" + "-".repeat(70));
            System.out.println("❌ FALTANTES NO CADASTRO (estão no financeiro, mas não no cadastro)");
            System.out.println("-".repeat(70));
            for (registroPlanilha reg : resultado.getFaltantesNoCadastro()) {
                System.out.println("  CPF: " + reg.getCpf() + " | Matrícula: " + reg.getMatricula() + " | Nome: " + reg.getNome());
            }
        }
        
        if (resultado.getTotalExcedentes() > 0) {
            System.out.println("\n" + "-".repeat(70));
            System.out.println("⚠️  EXCEDENTES NO CADASTRO (estão no cadastro, mas não no financeiro)");
            System.out.println("-".repeat(70));
            for (registroPlanilha reg : resultado.getExcedentesNoCadastro()) {
                System.out.println("  CPF: " + reg.getCpf() + " | Matrícula: " + reg.getMatricula() + " | Nome: " + reg.getNome());
            }
        }
        if (resultado.getTotalCancelados() > 0) {
            System.out.println("\n" + "-".repeat(70));
            System.out.println("⚠️  REGISTROS CANCELADOS NO CADASTRO (ignorados na comparação)");
            System.out.println("-".repeat(70));
            for (registroPlanilha reg : resultado.getCanceladosNoCadastro()) {
                 System.out.println("  CPF: " + reg.getCpf() + " | Matrícula: " + reg.getMatricula() + " | Nome: " + reg.getNome());
            }
        }
        
        if (resultado.getTotalDivergencias() > 0) {
            System.out.println("\n" + "-".repeat(70));
            System.out.println("🔄 DIVERGÊNCIAS POR REGISTRO");
            System.out.println("-".repeat(70));
            for (Map.Entry<String, List<Divergencia>> entry : resultado.getDivergenciasPorChave().entrySet()) {
                String[] partes = entry.getKey().split("\\|");
                String cpf = partes.length > 0 ? partes[0] : "";
                String matricula = partes.length > 1 ? partes[1] : "";
                System.out.println("\n  📌 CPF: " + cpf + " | Matrícula: " + matricula);
                for (Divergencia div : entry.getValue()) {
                    System.out.println(div);
                }
            }
        }
        
        if (resultado.getTotalConflitos() > 0) {
            System.out.println("\n" + "-".repeat(70));
            System.out.println("⚡ CONFLITOS (mesmo CPF, mas matrícula diferente)");
            System.out.println("-".repeat(70));
            System.out.println("  ⚠️  Atenção! O mesmo CPF aparece com matrículas diferentes");
            System.out.println("  em cada sistema. Verifique manualmente:\n");
            for (registroPlanilha reg : resultado.getConflitosCPFMatricula()) {
                System.out.println("  CPF: " + reg.getCpf() + " | Matrícula: " + reg.getMatricula() + " | Nome: " + reg.getNome());
            }
        }
        
        System.out.println("\n" + "=".repeat(70));
    }
}