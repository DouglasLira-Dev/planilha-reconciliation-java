package br.com.projeto.comparador.service;

import java.io.InputStream;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private static final Properties USUARIOS = new Properties();

    static {
        // Tenta carregar o arquivo de dentro do JAR (classpath)
        try (InputStream input = AuthService.class.getClassLoader()
                .getResourceAsStream("usuarios.properties")) {
            if (input == null) {
                System.err.println("Arquivo usuarios.properties não encontrado no classpath. Usando usuário padrão.");
                // Fallback: usuário admin padrão
                USUARIOS.setProperty("admin", "$2a$10$N9qo8uLOickgx2ZMRZoMy.MrqXpZc3O4ZqX1sXvK5Y9z1C5Z0Z0a");
            } else {
                USUARIOS.load(input);
                System.out.println("Credenciais carregadas com sucesso. Total de usuários: " + USUARIOS.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean autenticar(String login, String senha) {
        String hash = USUARIOS.getProperty(login);
        if (hash == null) return false;
        return BCrypt.checkpw(senha, hash);
    }

    // Opcional: retornar nome amigável (pode ser lido de outro arquivo ou deixar fixo)
    public static String getNomeUsuario(String login) {
        switch (login) {
            case "admin": return "Administrador";
            // Adicione outros casos conforme necessário
            default: return login;
        }
    }
}