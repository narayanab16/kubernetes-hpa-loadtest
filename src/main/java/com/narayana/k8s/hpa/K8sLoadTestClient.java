package com.narayana.k8s.hpa;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class K8sLoadTestClient {
    private WebClient webClient;

    public K8sLoadTestClient(WebClient webClient) {
        this.webClient = webClient;
    }
    private Mono<String> k8sHPALoadTestServer(K8sLoadTestClient client) {
        return this.webClient.get().uri("/").accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(x ->
                        x.statusCode().equals(HttpStatus.OK) ?
                                x.bodyToMono(String.class) :
                                Mono.justOrEmpty(null)
                );
    }

    private static void k8sHPALoadTest(K8sLoadTestClient client, int ftps, int load_it) {
        ExecutorService executorService = Executors.newFixedThreadPool(ftps);
        List<Future<String>> responseList = new ArrayList<>();
        try{
            for (int i = 0; i < load_it; i++) {
                Future<String> retValue = executorService.submit(() -> {
                    Mono<String> stringMono = client.k8sHPALoadTestServer(client);
                    String response = stringMono.block();
                    return response;
                });
                responseList.add(retValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        while (!executorService.isTerminated()){}
        AtomicLong counter = new AtomicLong();
        responseList.stream().forEach(x -> {
            try {
                String result = x.get();
                System.out.println(" result : " + result);
                counter.incrementAndGet();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Total requests : " + counter.get());
    }

    public static void main(String[] args) {
        WebClient webClient =
                WebClient.builder().baseUrl("http://localhost:8082").build();
        int FIXED_THREAD_POOL_SIZE = 20;
        int LOAD_ITERATIONS = 200000; // 100K iterations
        K8sLoadTestClient client = new K8sLoadTestClient(webClient);
        k8sHPALoadTest(client, FIXED_THREAD_POOL_SIZE, LOAD_ITERATIONS);
    }
}
