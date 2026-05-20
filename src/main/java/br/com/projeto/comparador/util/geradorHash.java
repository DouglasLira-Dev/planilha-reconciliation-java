package br.com.projeto.comparador.util;

import org.mindrot.jbcrypt.BCrypt;

public class geradorHash {
    public static void main(String[] args) {
        String[][] usuarios = {
            {"admin", "admin123"},
            {"leandro", "leo123"},
            {"silvana", "sil123"},
            {"monica", "monica123"},
            {"ralph", "ralph123"}
        };
        
        for (String[] user : usuarios) {
            String hash = BCrypt.hashpw(user[1], BCrypt.gensalt());
            System.out.println(user[0] + " = " + hash);
        }
    }
}