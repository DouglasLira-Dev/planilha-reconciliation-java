package br.com.projeto.comparador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.projeto.comparador.model.registroPlanilha;

public class comparadorPlanilhas {
    
    // Classe para armazenar divergências
    public static class Divergencia {
        private String chave;
        private String campo;
        private String valorFinanceiro;
        private String valorCadastro;
        
        public Divergencia(String chave, String campo, String valorFinanceiro, String valorCadastro) {
            this.chave = chave;
            this.campo = campo;
            this.valorFinanceiro = valorFinanceiro;
            this.valorCadastro = valorCadastro;
        }
        
        public String getChave() { return chave; }
        public String getCampo() { return campo; }
        public String getValorFinanceiro() { return valorFinanceiro; }
        public String getValorCadastro() { return valorCadastro; }
        
        @Override
        public String toString() {
            return String.format("  • %s: [Financeiro: %s] vs [Cadastro: %s]", 
                campo, valorFinanceiro, valorCadastro);
        }
    }
    
    // Classe para armazenar o resultado completo
    public static class ResultadoComparacao {
        private List<registroPlanilha> faltantesNoCadastro;
        private List<registroPlanilha> excedentesNoCadastro;
        private Map<String, List<Divergencia>> divergenciasPorChave;
        private int totalConformes;
        
        public ResultadoComparacao() {
            this.faltantesNoCadastro = new ArrayList<>();
            this.excedentesNoCadastro = new ArrayList<>();
            this.divergenciasPorChave = new HashMap<>();
            this.totalConformes = 0;
        }
        
        // Getters
        public List<registroPlanilha> getFaltantesNoCadastro() { return faltantesNoCadastro; }
        public List<registroPlanilha> getExcedentesNoCadastro() { return excedentesNoCadastro; }
        public Map<String, List<Divergencia>> getDivergenciasPorChave() { return divergenciasPorChave; }
        public int getTotalConformes() { return totalConformes; }
        public void setTotalConformes(int totalConformes) { this.totalConformes = totalConformes; }
        
        public int getTotalFaltantes() { return faltantesNoCadastro.size(); }
        public int getTotalExcedentes() { return excedentesNoCadastro.size(); }
        public int getTotalDivergencias() { return divergenciasPorChave.size(); }
        
        public void addFaltante(registroPlanilha reg) { faltantesNoCadastro.add(reg); }
        public void addExcedente(registroPlanilha reg) { excedentesNoCadastro.add(reg); }
        
        public void addDivergencia(String chave, Divergencia div) {
            divergenciasPorChave.computeIfAbsent(chave, k -> new ArrayList<>()).add(div);
        }
    }
    
    /**
     * Compara duas listas de registros (financeiro vs cadastro)
     * @param financeiro Lista da planilha do financeiro
     * @param cadastro Lista da planilha de cadastro
     * @return Resultado da comparação
     */
    public static ResultadoComparacao comparar(List<registroPlanilha> financeiro, List<registroPlanilha> cadastro) {
        ResultadoComparacao resultado = new ResultadoComparacao();
        
        // Criar mapas usando matrícula como chave (ou CPF se preferir)
        Map<String, registroPlanilha> mapFinanceiro = new HashMap<>();
        Map<String, registroPlanilha> mapCadastro = new HashMap<>();
        
        for (registroPlanilha reg : financeiro) {
            mapFinanceiro.put(reg.getMatriculaNorm(), reg);
        }
        
        for (registroPlanilha reg : cadastro) {
            mapCadastro.put(reg.getMatriculaNorm(), reg);
        }
        
        // 1. Verificar faltantes (está no financeiro, não está no cadastro)
        for (String chave : mapFinanceiro.keySet()) {
            if (!mapCadastro.containsKey(chave)) {
                resultado.addFaltante(mapFinanceiro.get(chave));
            }
        }
        
        // 2. Verificar excedentes (está no cadastro, não está no financeiro)
        for (String chave : mapCadastro.keySet()) {
            if (!mapFinanceiro.containsKey(chave)) {
                resultado.addExcedente(mapCadastro.get(chave));
            }
        }
        
        // 3. Verificar divergências (está nas duas, mas com diferenças)
        for (String chave : mapFinanceiro.keySet()) {
            if (mapCadastro.containsKey(chave)) {
                registroPlanilha regFin = mapFinanceiro.get(chave);
                registroPlanilha regCad = mapCadastro.get(chave);
                
                List<Divergencia> divergencias = new ArrayList<>();
                
                // Comparar cada campo
                if (!regFin.getCpfNorm().equals(regCad.getCpfNorm())) {
                    divergencias.add(new Divergencia(chave, "CPF", regFin.getCpfNorm(), regCad.getCpfNorm()));
                }
                
                if (!regFin.getNomeNorm().equals(regCad.getNomeNorm())) {
                    divergencias.add(new Divergencia(chave, "Nome", regFin.getNomeNorm(), regCad.getNomeNorm()));
                }
                
                if (!regFin.getNivelEstagioNorm().equals(regCad.getNivelEstagioNorm())) {
                    divergencias.add(new Divergencia(chave, "Nível Estágio", regFin.getNivelEstagioNorm(), regCad.getNivelEstagioNorm()));
                }
                
                if (!Objects.equals(regFin.getDataInicio(), regCad.getDataInicio())) {
                    divergencias.add(new Divergencia(chave, "Data Início", 
                        regFin.getDataInicio() != null ? regFin.getDataInicio().toString() : "null",
                        regCad.getDataInicio() != null ? regCad.getDataInicio().toString() : "null"));
                }
                
                if (!Objects.equals(regFin.getDataFim(), regCad.getDataFim())) {
                    divergencias.add(new Divergencia(chave, "Data Fim",
                        regFin.getDataFim() != null ? regFin.getDataFim().toString() : "null",
                        regCad.getDataFim() != null ? regCad.getDataFim().toString() : "null"));
                }
                
                if (!regFin.getBancoNorm().equals(regCad.getBancoNorm())) {
                    divergencias.add(new Divergencia(chave, "Banco", regFin.getBancoNorm(), regCad.getBancoNorm()));
                }
                
                if (!regFin.getAgenciaNorm().equals(regCad.getAgenciaNorm())) {
                    divergencias.add(new Divergencia(chave, "Agência", regFin.getAgenciaNorm(), regCad.getAgenciaNorm()));
                }
                
                if (!regFin.getContaNorm().equals(regCad.getContaNorm())) {
                    divergencias.add(new Divergencia(chave, "Conta", regFin.getContaNorm(), regCad.getContaNorm()));
                }
                
                if (!divergencias.isEmpty()) {
                    for (Divergencia div : divergencias) {
                        resultado.addDivergencia(chave, div);
                    }
                } else {
                    resultado.setTotalConformes(resultado.getTotalConformes() + 1);
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * Exibe o resultado da comparação no console
     */
    public static void exibirResultado(ResultadoComparacao resultado) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESUMO DA COMPARAÇÃO");
        System.out.println("=".repeat(60));
        System.out.println("✅ Registros conformes (idênticos): " + resultado.getTotalConformes());
        System.out.println("❌ Registros faltantes no cadastro: " + resultado.getTotalFaltantes());
        System.out.println("⚠️  Registros excedentes no cadastro: " + resultado.getTotalExcedentes());
        System.out.println("🔄 Registros com divergências: " + resultado.getTotalDivergencias());
        
        if (resultado.getTotalFaltantes() > 0) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("❌ FALTANTES NO CADASTRO (estão no financeiro, mas não no sistema)");
            System.out.println("-".repeat(60));
            for (registroPlanilha reg : resultado.getFaltantesNoCadastro()) {
                System.out.println("  Matrícula: " + reg.getMatricula() + " | Nome: " + reg.getNome());
            }
        }
        
        if (resultado.getTotalExcedentes() > 0) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("⚠️  EXCEDENTES NO CADASTRO (estão no sistema, mas não no financeiro)");
            System.out.println("-".repeat(60));
            for (registroPlanilha reg : resultado.getExcedentesNoCadastro()) {
                System.out.println("  Matrícula: " + reg.getMatricula() + " | Nome: " + reg.getNome());
            }
        }
        
        if (resultado.getTotalDivergencias() > 0) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("🔄 DIVERGÊNCIAS POR REGISTRO");
            System.out.println("-".repeat(60));
            for (Map.Entry<String, List<Divergencia>> entry : resultado.getDivergenciasPorChave().entrySet()) {
                System.out.println("\n  Matrícula: " + entry.getKey());
                for (Divergencia div : entry.getValue()) {
                    System.out.println(div);
                }
            }
        }
        
        System.out.println("\n" + "=".repeat(60));
    }
}