import groovy.json.JsonSlurper

throw new RuntimeException('Fill these in first')
def azStorageAcct = null
def adlsContainer = null
def sasToken = null

// https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/list
def baseUrl =
"https://${azStorageAcct}.dfs.core.windows.net/${adlsContainer}?${sasToken}&recursive=true&resource=filesystem"


def continuation = null
do {

    def url = baseUrl
    if (continuation) {
        continuation = URLEncoder.encode(continuation, 'UTF-8') 
        url += "&continuation=${continuation}"
    }
    def connection = new URL(url).openConnection() as HttpURLConnection

    connection.setRequestProperty( 'User-Agent', 'groovy-2.4.4' )
    connection.setRequestProperty( 'Accept', 'application/json' )

    // get the response code - automatically sends the request
    if ( connection.responseCode == 200 ) {
        // get the JSON response
        def json = connection.inputStream.withCloseable { inStream ->
            new JsonSlurper().parse( inStream as InputStream )
        }

        json.paths.each {
            // for example, print path if a file is .xml or .xml.gz
            if (it.name ==~ /.*\/1_.*\.xml(\.gz)?/) {
                println it.name
            }
        }

        continuation = connection.headerFields['x-ms-continuation'][0]

    } else {
        println connection.responseCode + ": " + connection.inputStream.text
        continuation = false
    }

} while (continuation)
