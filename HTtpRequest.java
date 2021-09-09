HttpHeaders headers = new HttpHeaders();
headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("msisdn", msisdn)
        .queryParam("email", email)
        .queryParam("clientVersion", clientVersion)
        .queryParam("clientType", clientType)
        .queryParam("issuerName", issuerName)
        .queryParam("applicationName", applicationName);

HttpEntity<?> entity = new HttpEntity<>(headers);

HttpEntity<String> response = restTemplate.exchange(
        builder.toUriString(), 
        HttpMethod.GET, 
        entity, 
        String.class);





/// another way
HttpHeaders headers = new HttpHeaders();
headers.set("Accept", "application/json");

Map<String, String> params = new HashMap<String, String>();
params.put("msisdn", msisdn);
params.put("email", email);
params.put("clientVersion", clientVersion);
params.put("clientType", clientType);
params.put("issuerName", issuerName);
params.put("applicationName", applicationName);

HttpEntity entity = new HttpEntity(headers);

HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, params);
