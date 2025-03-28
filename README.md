# TranslateOCRApp: 

TranslateOCRApp is an Android application that allows users to click pictures of Swedish and German text and translate
them into english
=======
TranslateOCRApp is an Android application that allows users to click pictures of  German text and translate
them into english. The app leverages [Google ML-Kit](https://developers.google.com/ml-kit) for ML functionalities like OCR, Language identification and Translation and [CameraX](https://developer.android.com/training/camerax) for camera functionality.

This is a companion repo for by blog: [Android OCR Translation App using Kotlin and Google ML-Kit](https://jayeshmahapatra.github.io/2023/07/27/translation-app.html)

## Installing the App

You can either download the apk from the [releases](https://github.com/jayeshmahapatra/TranslateOCRApp/releases) or build the apk yourself using Android Studio.

## How to Use

Point the camera at the text you want to translate and capture an image by clicking the capture button. After the capture, the app will translate the text and display an image with the translated text.

**Note**: The app needs wifi internet the first time it's used to download and store the translation models.

<figure>
    <img src="media/translation_app_example_use.gif"
         alt="A gif of using the translation app to translate an advertisement poster"
         width = "270"
         height = "500">
    <figcaption>Translating an advertisement poster using the app</figcaption>
</figure>

## Workflow

The TranslateOCRApp works in the following steps:

1. **Capture Image:** The user can capture an image of the text using the camera functionality implemented in `MainActivity`. Upon capturing the image, it is saved for further processing.

2. **Optical Character Recognition (OCR):** After the image is captured, the `PreviewActivity` is launched, where the raw image is displayed. The app then utilizes Google ML Kit's OCR capabilities provided by the `OcrHelper` class to extract all the text present in the image.

3. **Language Identification:** Once the text is extracted using OCR, the `LanguageRecognizer` class is employed to identify the language of the extracted text. The app determines if the text is in Swedish, German, or an undetermined language.

4. **Translation:** Based on the identified language, the app decides which language translation model to use. The `TextTranslator` class handles the downloading and loading of the appropriate translation model (German if undetermined). The text is then translated into English. The class also manages translation models, such as downloading and storing them locally as needed.

5. **Image Transformation:** The original image, along with the overlay of the translated text, is displayed to the user in the `PreviewActivity`. The `BitmapAnnotator` class takes care of overlaying the translated text on top of the original image. This is done by blurring the original text and replacing them with their translations.


## Contribution

Contributions to the TranslateOCRApp are welcome! If you find any issues or want to add new features, please create a pull request or open an issue in this GitHub repository.

## License

The TranslateOCRApp is released under the [MIT License](LICENSE). Feel free to use, modify, and distribute the code as per the terms of the license.

