package com.codefathers.util;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class CepUtils {
    public static String getStateByCep(String cep) throws Exception {
        URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (Scanner scanner = new Scanner(new InputStreamReader(conn.getInputStream()))) {
            String response = scanner.useDelimiter("\\A").next();
            int ufIndex = response.indexOf("\"uf\":");
            if (ufIndex != -1) {
                int start = response.indexOf("\"", ufIndex + 5) + 1;
                int end = response.indexOf("\"", start);
                return response.substring(start, end);
            }
        }

        throw new Exception("UF não encontrada para o CEP");
    }
}
