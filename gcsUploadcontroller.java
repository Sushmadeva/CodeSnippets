@RestController
@Slf4j
class StorageController {

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    @Autowired
    private Storage storage;

    @Value("bucketname")
    String bucketName;
    @Value("subdirectory")
    String subdirectory;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<URL> uploadFile(@RequestPart("file") FilePart filePart) {
        //Convert the file to a byte array
        final byte[] byteArray = convertToByteArray(filePart);

        //Prepare the blobId
        //BlobId is a combination of bucketName + subdirectiory(optional) + fileName
        final BlobId blobId = constructBlobId(bucketName, subdirectory, filePart.filename());

        return Mono.just(blobId)
                //Create the blobInfo
                .map(bId -> BlobInfo.newBuilder(blobId)
                        .setContentType("text/plain")
                        .build())
                //Upload the blob to GCS
                .doOnNext(blobInfo -> getStorage().create(blobInfo, byteArray))
                //Create a Signed "Path Style" URL to access the newly created Blob
                //Set the URL expiry to 10 Minutes
                .map(blobInfo -> createSignedPathStyleUrl(blobInfo, 10, TimeUnit.MINUTES));
    }

    private URL createSignedPathStyleUrl(BlobInfo blobInfo,
                                         int duration, TimeUnit timeUnit) {
        return getStorage()
                .signUrl(blobInfo, duration, timeUnit, Storage.SignUrlOption.withPathStyle());
    }

    /**
     * Construct Blob ID
     *
     * @param bucketName
     * @param subdirectory optional
     * @param fileName
     * @return
     */
    private BlobId constructBlobId(String bucketName, @Nullable String subdirectory,
                                   String fileName) {
        return Optional.ofNullable(subdirectory)
                .map(s -> BlobId.of(bucketName, subdirectory + "/" + fileName))
                .orElse(BlobId.of(bucketName, fileName));
    }

    /**
     * Here, we convert the file to a byte array to be sent to GCS Libraries
     *
     * @param filePart File to be used
     * @return Byte Array with all the contents of the file
     */
    @SneakyThrows
    private byte[] convertToByteArray(FilePart filePart) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            filePart.content()
                    .subscribe(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        log.trace("readable byte count:" + dataBuffer.readableByteCount());
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        try {
                            bos.write(bytes);
                        } catch (IOException e) {
                            log.error("read request body error...", e);
                        }
                    });

            return bos.toByteArray();
        }
    }

}
Lets break up the above code and understand each part.
First, we convert the FilePart file to an Array of Bytes.
/**
 * Here, we convert the file to a byte array to be sent to GCS Libraries
 *
 * @param filePart File to be used
 * @return Byte Array with all the contents of the file
 */
@SneakyThrows
private byte[] convertToByteArray(FilePart filePart) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        filePart.content()
                .subscribe(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    log.trace("readable byte count:" + dataBuffer.readableByteCount());
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    try {
                        bos.write(bytes);
                    } catch (IOException e) {
                        log.error("read request body error...", e);
                    }
                });

        return bos.toByteArray();
    }
}
