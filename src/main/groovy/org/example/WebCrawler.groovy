package org.example

import java.text.SimpleDateFormat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.nio.file.Files
import java.nio.file.Paths

class WebCrawler {
    private final String baseUrl = 'https://www.gov.br/ans/pt-br'
    private final OkHttpClient client = new OkHttpClient()

    static void main(String[] args) {
        def crawler = new WebCrawler()
        crawler.downloadTISSFiles()
    }

    void downloadTISSFiles() {
        try {
            // Faz a requisição HTTP para a página alvo
            def request = new Request.Builder()
                    .url("$baseUrl/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/julho-2024")
                    .build()

            Response response = client.newCall(request).execute()
            if (!response.isSuccessful()) {
                println "Falha ao buscar a página: ${response}"
                return
            }

            // Parseia a resposta HTML usando Jsoup
            Document doc = Jsoup.parse(response.body().string())

            // Encontra os links na página
            Elements links = doc.select("a[href]")

            // Gera um timestamp para o nome do arquivo
            def dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
            def now = new Date()
            def timestamp = dateFormat.format(now)

            // Procura os links que contêm 'Comunicao'
            links.each { link ->
                String href = link.attr('href')
                if (href.contains('Comunicao')) {
                    // Adapta o URL para caso o href seja relativo
                    String fileUrl = href.startsWith('http') ? href : "$baseUrl${href}"
                    println "Iniciando download do arquivo: $fileUrl"

                    // Adiciona o timestamp ao nome do arquivo
                    String destination = "./Downloads/comunicacao_${timestamp}.zip"
                    downloadFile(fileUrl, destination)
                }
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    static void downloadFile(String fileUrl, String destination) {
        try {
            def request = new Request.Builder()
                    .url(fileUrl)
                    .build()

            Response response = new OkHttpClient().newCall(request).execute()

            if (!response.isSuccessful()) {
                println "Falha ao baixar o arquivo: ${response}"
                return
            }

            Files.createDirectories(Paths.get(destination).getParent()) // Garante que o diretório exista
            Files.copy(response.body().byteStream(), Paths.get(destination))
            println "Arquivo baixado em: $destination"
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}
