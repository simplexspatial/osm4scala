
Looking for HeaderBlo

```
00000080  __ __ __ __ __ __ __ __  __ __ __ __ __ 00 00 00
00000090  0d - length in bytes of the BlobHeader in network-byte order
00000090  __ 0a - S 1 'type'
00000090  __ __ 07 - length 7 bytes
00000090  __ __ __ 4f 53 4d 44 61  74 61 "OSMData"
00000090  __ __ __ __ __ __ __ __  __ __ 18 - V 3 'datasize'
00000090  __ __ __ __ __ __ __ __  __ __ __ 90 af 05 - 87952 bytes long
00000090  __ __ __ __ __ __ __ __  __ __ __ __ __ __ 10 - V 2 'raw_size'
00000090  __ __ __ __ __ __ __ __  __ __ __ __ __ __ __ 8f
000000a0  84 08 - 131599 bytes long
000000a0  __ __ 1a - S 3 'zlib_data'
000000a0  __ __ __ 88 af 05 - length 87944 bytes
```

Hexadecimal patter to search? 6 bytes + `4F 53 4D 44 61 74 61`



```
00000000  00 00 00 0d - length in bytes of the BlobHeader in network-byte order
00000000  __ __ __ __ 0a - S 1 'type'
00000000  __ __ __ __ __ 09 - length 9 bytes
00000000  __ __ __ __ __ __ 4f 53  4d 48 65 61 64 65 72 - "OSMHeader"
00000000  __ __ __ __ __ __ __ __  __ __ __ __ __ __ __ 18 - V 3 'datasize'
00000010  7c - 124 bytes long
00000010  __ 10 - V 2 'raw_size'
00000010  __ __ 71 - 113 bytes long
00000010  __ __ __ 1a - S 3 'zlib_data'
00000010  __ __ __ __ 78 - length 120 bytes
``` 

Hexadecimal patter to search? 6 bytes + `4f 53  4d 48 65 61 64 65 72`

After find, go back 6 bytes and try to parse it as a blob header.