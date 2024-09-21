package org.example

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

class WebCrawler {
    private final String baseUrl = 'https://www.gov.br/ans/pt-br'
    private final OkHttpClient client = new OkHttpClient()

    void downloadSendingErrorsTable() {
        try {
            // Faz a requisição HTTP para a página com tabelas relacionadas
            def request = new Request.Builder()
                    .url("$baseUrl/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/padrao-tiss-tabelas-relacionadas")
                    .build()

            Response response = client.newCall(request).execute()
            if (!response.isSuccessful()) {
                println "Falha ao buscar a página de tabelas relacionadas: ${response}"
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
                if (href.contains('Tabelaerrosenvioparaanspadraotiss')) {
                    // Adapta o URL para caso o href seja relativo
                    String fileUrl = href.startsWith('http') ? href : "$baseUrl$href"
                    println "Iniciando download do arquivo: $fileUrl"

                    // Adiciona o timestamp ao nome do arquivo
                    String destination = "./Downloads/tabela_de_erros_${timestamp}.xlsx"
                    downloadFile(fileUrl, destination)
                }
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }


    void downloadComunicationComponent() {
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
                if (href.contains('PadroTISSComunicao')) {
                    // Adapta o URL para caso o href seja relativo
                    String fileUrl = href.startsWith('http') ? href : "$baseUrl$href"
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

    void collectVersionHistory() {
        try {
            def request = new Request.Builder()
                    .url("$baseUrl/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/padrao-tiss-historico-das-versoes-dos-componentes-do-padrao-tiss")
                    .build()

            Response response = client.newCall(request).execute()
            if (!response.isSuccessful()) {
                println "Falha ao buscar a página de histórico: ${response}"
                return
            }

            // Parseia a resposta HTML usando Jsoup
            Document doc = Jsoup.parse(response.body().string())

            // Seleciona a tabela de histórico (modifique o seletor conforme necessário)
            Elements table = doc.select("table") // Ajustar seletor da tabela conforme necessário
            Elements rows = table.select("tr")

            // Cria o arquivo CSV
            def csvFile = "./Downloads/tabela_dados.csv"
            FileWriter writer = new FileWriter(csvFile)
            writer.write("Competência,Publicação,Início de Vigência,Limite de Implantação,Organizacional,Conteúdo e Estrutura,Representação de Conceitos,Segurança e Privacidade,Comunicação\n")

            // Itera pelas linhas da tabela
            for (int i = 0; i < rows.size(); i++) {
                Elements cells = rows.get(i).select("td")
                if (!cells.isEmpty()) {
                    // Extrai os dados das células
                    def competencia = cells.get(0).text().capitalize()
                    def publicacao = cells.get(1).text().capitalize()
                    def inicioVigencia = cells.get(2).text().capitalize()
                    def limiteImplantacao = cells.get(3).text().capitalize()
                    def organizacional = cells.get(4).text().capitalize()
                    def conteudoEstrutura = cells.get(5).text().capitalize()
                    def representacaoConceitos = cells.get(6).text().capitalize()
                    def segurancaPrivacidade = cells.get(7).text().capitalize()
                    def comunicacao = cells.get(8).text().capitalize()

                    // Escreve os dados no CSV
                    writer.write("$competencia,$publicacao,$inicioVigencia,$limiteImplantacao,$organizacional,$conteudoEstrutura,$representacaoConceitos,$segurancaPrivacidade,$comunicacao\n")

                    if (competencia == "Jan/2016") {
                        break
                    }
                }
            }

            writer.close()
            println "Dados da tabela extraídos e salvos em: $csvFile"
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}
