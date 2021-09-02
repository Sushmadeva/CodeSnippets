            final String fileName = fileStream.getName() + dtString;

                File file = convertMultiPartToFile( fileStream );

                BlobInfo blobInfo =
                        storage.create(
                                BlobInfo
                                        .newBuilder(bucketName, fileName)
                                        .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
                                        .build()
        //                     file.openStream() 
        );
                System.out.println(blobInfo.getMediaLink());
                return blobInfo.getMediaLink();
            }


            private File convertMultiPartToFile(MultipartFile file ) throws IOException
            {
                File convFile = new File( file.getOriginalFilename() );
                FileOutputStream fos = new FileOutputStream( convFile );
                fos.write( file.getBytes() );
                fos.close();
                return convFile;
            }


            private void checkFileExtension(String fileName) throws ServletException {
                if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
                    String[] allowedExt = {".jpg", ".jpeg", ".png", ".gif"};
                    for (String ext : allowedExt) {
                        if (fileName.endsWith(ext)) {
                            return;
                        }
                    }
                    throw new ServletException("file must be an image");
                }
            }
