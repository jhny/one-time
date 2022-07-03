(ns one-time.qrgen
  (:require [one-time.uri :as uri])
  (:import
    (com.google.zxing BarcodeFormat)
    (com.google.zxing.client.j2se MatrixToImageWriter)
    (com.google.zxing.qrcode QRCodeWriter)
    (java.io File)))

(defn- create-tmp-file [type]
  (let [tmp (File/createTempFile "qrcode" (str "." type))]
    (.deleteOnExit tmp)
    tmp))

(defn- write-qr-code-to-file [text image-size type]
  (let [writer (QRCodeWriter.)
        bit-matrix (.encode writer text BarcodeFormat/QR_CODE image-size image-size)
        ^File file (create-tmp-file type)]
    (MatrixToImageWriter/writeToPath bit-matrix type (.toPath file))
    file))

(defn- write-qr-code-to-stream [text image-size type]
  (let [writer (QRCodeWriter.)
        bit-matrix (.encode writer text BarcodeFormat/QR_CODE image-size image-size)
        output-stream (java.io.ByteArrayOutputStream.)]
    (MatrixToImageWriter/writeToStream bit-matrix type output-stream)
    output-stream))

(def ^:private image-types
  {:JPG "jpg"
   :GIF "gif"
   :PNG "png"
   :BMP "bmp"})

(defn totp-stream
  "Returns a java.io.ByteArrayOutputStream with the totp qrcode"
  [{:keys [image-type image-size label user secret]
    :or   {image-type :JPG image-size 125}}]
  {:pre [(not-any? nil? [label user secret])
         (image-types image-type)]}
  (let [text (uri/totp-uri {:label  label
                            :secret secret
                            :user   user})
        image-type (image-types image-type)]
    (write-qr-code-to-stream text image-size image-type)))

(defn totp-file
  "Returns a java.io.File with the totp qrcode"
  [{:keys [image-type image-size label user secret]
    :or   {image-type :JPG image-size 125}}]
  {:pre [(not-any? nil? [label user secret])
         (image-types image-type)]}
  (let [text (uri/totp-uri {:label  label
                            :secret secret
                            :user   user})
        image-type (image-types image-type)]
    (write-qr-code-to-file text image-size image-type)))

(defn hotp-stream
  "Returns a java.io.ByteArrayOutputStream with the hotp qrcode"
  [{:keys [image-type image-size label user secret counter]
    :or   {image-type :JPG image-size 125}}]
  {:pre [(not-any? nil? [label user secret counter])
         (image-types image-type)]}
  (let [text (uri/hotp-uri {:label   label
                            :secret  secret
                            :user    user
                            :counter counter})
        image-type (image-types image-type)]
    (write-qr-code-to-stream text image-size image-type)))

(defn hotp-file
  "Returns a java.io.File with the hotp qrcode"
  [{:keys [image-type image-size label user secret counter]
    :or   {image-type :JPG image-size 125}}]
  {:pre [(not-any? nil? [label user secret counter])
         (image-types image-type)]}
  (let [text (uri/hotp-uri {:label   label
                            :secret  secret
                            :user    user
                            :counter counter})
        image-type (image-types image-type)]
    (write-qr-code-to-file text image-size image-type)))
