# 📊 Comparador de Planilhas de Estágios

Sistema desenvolvido em **Java** para comparar dados de estagiários entre duas planilhas Excel (Financeiro vs Cadastro), identificar divergências, gerar relatórios e manter histórico de comparações. Possui interface gráfica (JavaFX) e versão console, com autenticação de usuários e filtros por período.

## 📌 Índice

- [Sobre o projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias utilizadas](#-tecnologias-utilizadas)
- [Pré‑requisitos](#-pré‑requisitos)
- [Configuração e execução](#-configuração-e-execução)
  - [Clonar o repositório](#1-clonar-o-repositório)
  - [Compilar com Maven](#2-compilar-com-maven)
  - [Executar a interface gráfica](#3-executar-a-interface-gráfica)
  - [Executar a versão console](#4-executar-a-versão-console)
  - [Gerar um executável nativo (.exe)](#5-gerar-um-executável-nativo-exe)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Como usar](#-como-usar)
  - [Login](#login)
  - [Selecionar planilhas](#selecionar-planilhas)
  - [Filtro por período](#filtro-por-período)
  - [Resultados e relatório Excel](#resultados-e-relatório-excel)
- [Melhorias implementadas](#-melhorias-implementadas)
- [Contribuição](#-contribuição)
- [Licença](#-licença)

---

## 📖 Sobre o projeto

Este projeto surgiu da necessidade de reconciliar mensalmente os dados de estagiários entre o setor financeiro (planilha com contratos ativos) e o setor de cadastro (planilha com registros de estágio). O sistema lê duas planilhas Excel, normaliza os dados (CPF, datas, nomes, agências, etc.) e as compara usando uma chave composta (CPF + Matrícula). Ele identifica:

- Registros conformes (idênticos em ambas)
- Registros faltantes no cadastro (presentes no financeiro)
- Registros excedentes no cadastro (presentes apenas no cadastro)
- Divergências campo a campo (com classificação ERRO ou AVISO)
- Possíveis abreviações nos nomes
- Registros cancelados (quando a data de início contém "CANCELADO")
- Conflitos de CPF com matrícula diferente

Além disso, gera um relatório Excel detalhado com várias abas e permite autenticação de usuários via arquivo `.properties`.

---

## ✨ Funcionalidades

- **Leitura flexível de Excel**  
  Detecta cabeçalhos por sinônimos (ignora maiúsculas/minúsculas, acentos, pontuação). Suporta qualquer ordem de colunas e colunas extras.

- **Normalização inteligente**  
  - CPF: remove pontos/traços e completa com zeros à esquerda (11 dígitos).  
  - Agência/Conta: padroniza para 5 dígitos (com zeros à esquerda).  
  - Nomes: remove acentos, converte para minúsculo, compara similaridade (distância de Levenshtein).  
  - Datas: aceita múltiplos formatos (dd/MM/yyyy, número do Excel, etc.).

- **Comparação avançada**  
  - Chave composta: `CPF normalizado + "|" + Matrícula normalizada`.  
  - Similaridade de nomes com limiar configurável (85%).  
  - Detecção de erro de digitação (diferença de 1‑2 caracteres) vs abreviação.  
  - Campos comparados: matrícula, CPF, nome, nível de estágio, datas, banco, agência, conta.

- **Interface gráfica (JavaFX)**  
  - Tela de login com autenticação BCrypt.  
  - Seleção de arquivos via `FileChooser`.  
  - Barra de progresso e thread separada para não travar a UI.  
  - Exibição de resultados em área de texto com formatação.

- **Relatório Excel (Apache POI)**  
  - Abas: Resumo, Faltantes, Excedentes, Divergências, Conflitos, Possíveis Abreviações, Conformes, Cancelados, Detalhado.  
  - Cores condicionais (vermelho para erro, amarelo para aviso).  
  - Escolha do nome do arquivo e pasta de destino.

- **Filtro por período**  
  Permite filtrar os registros do financeiro por uma data de início mínima (ex: contratos iniciados a partir de maio/2025). Útil para planilhas com histórico completo.

- **Autenticação de usuários**  
  Credenciais armazenadas em `usuarios.properties` (hashes BCrypt). Administração simples sem necessidade de banco de dados centralizado.

- **Executável autônomo**  
  Gera um `.exe` com `jpackage` que inclui JRE personalizada – roda em qualquer Windows sem instalar Java.

---

## 🛠️ Tecnologias utilizadas

| Componente | Tecnologia | Finalidade |
|------------|------------|-------------|
| Linguagem | Java 21 | Lógica principal |
| Build | Maven 3.9+ | Gerenciamento de dependências e build |
| Interface | JavaFX 21 | Interface gráfica |
| Leitura de Excel | Apache POI 5.2.5 | Extração de dados de `.xlsx`/`.xls` |
| Normalização | Java padrão (java.text.Normalizer) | Remoção de acentos |
| Similaridade | Distância de Levenshtein (implementada) | Comparação fuzzy de nomes |
| Hashing de senhas | BCrypt (jBCrypt 0.4) | Autenticação segura |
| Logs | Log4j2 2.21.1 | Logs internos do Apache POI |
| JSON | Gson 2.10.1 | (não utilizado ativamente, reservado) |
| Empacotamento | jpackage (JDK 21) | Gerador de executável nativo |

---

## 📋 Pré‑requisitos

Para **desenvolvimento** (executar via Maven):

- JDK 21 instalado e configurado (`JAVA_HOME`)
- Maven 3.9+ (ou use o wrapper `mvnw`)
- Git (opcional, para clonar)

Para **distribuição** (executar o `.exe`):

- Nenhum – o executável é autônomo.

---

## ⚙️ Configuração e execução

### 1. Clonar o repositório

``bash
git clone https://github.com/seu-usuario/comparador-planilhas-estagios.git
cd comparador-planilhas-estagios
``

### 2. Compilar com Maven

``bash
mvn clean compile
``

### 3. Executar a interface gráfica

``bash
mvn javafx:run
``

### 4. Executar a versão console

``bash
mvn exec:java
``

### 5. Gerar um executável nativo (.exe)

Primeiro, baixe o **JavaFX SDK** (versão 21) em [gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/) e extraia.  
Depois, crie uma pasta apenas com os `.jmod` (copie todos os `.jmod` de `lib` para uma pasta separada, ex: `C:\javafx-jmods-21`).  

Em seguida, execute:

``bash
mvn clean package
jpackage --input target --name ComparadorEstagios --main-jar comparador-estagios-1.0-SNAPSHOT.jar --main-class br.com.projeto.comparador.interfaceApp --module-path "C:\javafx-jmods-21" --add-modules javafx.controls,javafx.fxml --type app-image --win-console
``

A pasta `ComparadorEstagios` será criada com o `.exe`. Compacte‑a para distribuição.

---

## 📁 Estrutura do projeto

comparador-planilhas-estagios/
├── src/
│ ├── main/
│ │ ├── java/br/com/projeto/comparador/
│ │ │ ├── interfaceApp.java # Interface gráfica (JavaFX)
│ │ │ ├── app.java # Versão console (legado)
│ │ │ ├── comparadorPlanilhas.java # Lógica de comparação
│ │ │ ├── geradorRelatorioExcel.java # Geração do Excel
│ │ │ ├── model/registroPlanilha.java# Modelo de dados
│ │ │ ├── reader/leitorPlanilha.java # Leitura de Excel
│ │ │ ├── util/normalizacao.java # Funções de normalização
│ │ │ ├── service/AuthService.java # Autenticação de usuários
│ │ │ └── database/ (opcional) # Histórico SQLite
│ │ └── resources/
│ │ └── usuarios.properties # Credenciais (login=hash)
│ └── test/ (não implementado)
├── pom.xml # Configuração Maven
├── .gitignore
└── README.md


---

## 🖥️ Como usar

### Login

Na primeira execução, use as credenciais padrão (você deve definir no `usuarios.properties`).  
Exemplo de linha no arquivo:  
`admin = $2a$10$N9qo8uLOickgx2ZMRZoMy.MrqXpZc3O4ZqX1sXvK5Y9z1C5Z0Z0a`  (senha: admin123)

### Selecionar planilhas

Clique em **Buscar** ao lado de cada campo e escolha os arquivos `.xlsx` ou `.xls`. A ordem das colunas não importa (leitura por cabeçalho). O sistema aceita sinônimos como "Prontuario" para matrícula, "DT ADMISSAO" para data de início, etc.

### Filtro por período

Após carregar o financeiro, o programa pergunta se deseja filtrar por uma data de início mínima. Informe um mês/ano (ex: `05/2025`) – serão mantidos apenas contratos com `dataInicio >= primeiro dia do mês`.

### Resultados e relatório Excel

Após a comparação, um resumo é exibido na tela. Se optar por gerar o relatório Excel, escolha um nome e uma pasta de destino. O arquivo conterá as seguintes abas:

| Aba | Conteúdo |
|-----|----------|
| 01 - Resumo | Estatísticas gerais, datas, limiar de similaridade |
| 02 - Faltantes no Cadastro | Registros do financeiro não encontrados no cadastro |
| 03 - Excedentes no Cadastro | Registros do cadastro não encontrados no financeiro |
| 04 - Divergências | Campos diferentes (com cores: vermelho=erro, amarelo=aviso) |
| 05 - Conflitos CPF e Matrícula | Mesmo CPF com matrícula diferente |
| 06 - Possíveis Abreviações | Nomes com alta similaridade mas que podem ser abreviações |
| 07 - Registros Conformes | Registros idênticos nas duas planilhas |
| 08 - Cancelados no Cadastro | Registros cuja data de início contém "CANCELADO" |
| 09 - Comparação Detalhada | (em desenvolvimento) |

---

## 🚀 Melhorias implementadas

- **Leitura por cabeçalho** – não depende da ordem das colunas.
- **Normalização de CPF e agência** – garante 11 e 5 dígitos (zeros à esquerda).
- **Similaridade fuzzy** – evita falsos negativos por erros de digitação ou abreviações.
- **Filtro por período** – permite analisar apenas contratos ativos a partir de uma data.
- **Autenticação de usuários** – controle de acesso com hashes BCrypt.
- **Executável autônomo** – gera um `.exe` que roda sem Java instalado.
- **Abas separadas no Excel** – melhora a organização dos resultados.
- **Tratamento de registros cancelados** – são listados separadamente, não interferem na comparação.

---

## 🤝 Contribuição

Contribuições são bem‑vindas! Sinta‑se à vontade para abrir issues ou pull requests. Antes de propor mudanças, verifique se o código está de acordo com as boas práticas Java e com a estrutura existente.

---

## 📄 Licença

Este projeto é licenciado sob a **MIT License** – consulte o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👨‍💻 Desenvolvido por

Douglas Lira – 2026

---

**Observação:** Para utilizar o executável `.exe` gerado, basta descompactar a pasta e executar `ComparadorEstagios.exe`. Não é necessário instalar Java, Maven ou qualquer outra dependência.