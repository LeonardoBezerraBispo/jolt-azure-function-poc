package com.jolt;

import java.util.*;

import com.bazaarvoice.jolt.*;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * @author: Leonardo Bezerra
 * @link: https://github.com/LeonardoBezerraBispo/jolt-azure-function-poc
 * @since: 23/12/2022
 */
public class Function {

    @FunctionName("function-jolt")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                route = "jolt-poc",
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            return request.createResponseBuilder(HttpStatus.OK)
            .body(transformJSONfromSPEC(request.getBody().orElse("{}"), "/specs/spec.json")).build();
        } catch (Exception e) {
            context.getLogger().info(e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Recebe uma String contendo JSON "payload", uma String contendo o path para o arquivo spec JSON "specPath"
     * e performa a transformação
     * 
     * @param payload
     * @param specPath
     * @return Object contendo o JSON transformado
     */
    private Object transformJSONfromSPEC(String payload, String specPath) {

        /*
         * Pegando a list no spec pelo class path definido 
         * no parâmetro "specClassPath" do método
         */
        List<Object> chainrSpecJSON = JsonUtils.classpathToList(specPath);

        /*
         * Encorrentando operações definidas no spec a partir da lista
         */
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

        /*
         * Importando String definido no parâmetro "json" do método 
         */
        Object inputJSON = JsonUtils.jsonToObject(payload);

        /*
         * Executando transformação utilizando json "inputJSON" 
         * a partir da corrente de operações definidas no spec importado
         */
        Object transformedOutput = chainr.transform(inputJSON);

        /*
         * Retornando objeto transformado
         */
        return transformedOutput;
    }
}
