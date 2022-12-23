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
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/jolt-poc". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/jolt-poc
     */
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
            context.getLogger().info(e.getMessage().toString());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Object transformJSONfromSPEC(String json, String specClassPath) {

        /*
         * Pegando a list no spec pelo class path definido 
         * no parâmetro "specClassPath" do método
         */
        List<Object> chainrSpecJSON = JsonUtils.classpathToList(specClassPath);

        /*
         * Encorrentando operações definidas no spec a partir da lista
         */
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

        /*
         * Importando String definido no parâmetro "json" do método 
         */
        Object inputJSON = JsonUtils.jsonToObject(json);

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
