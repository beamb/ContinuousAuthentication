# Continuous authentication using "something only you can do"
This repository contains the full code-base – including a recorded demonstration of the application – of a simple prototype implemented as a proof of concept for the thesis submitted in fulfillment of the requirements for the degree of Master of Science in Software Design at the IT University of Copenhagen.

## Authors
- [**Astrid Fischer Nielsen**](https://github.com/astridfischer)
- [**Beatrice Ambrosi de Magistris Verzier**](https://github.com/beamb)
- [**Simona Stegmann Holst**](https://github.com/SimonaStegmannHolst)

## Abstract
Strong and accurate digital user authentication is crucial as more systems move online to be accessed from personal devices. Contributing to this accuracy is also the assurance that the user interacting and performing decisions within the system is, in fact, the authenticated individual. This thesis explores the possibility of continuous authentication using biometrics, introducing a second authentication factor, "something only you can do", to reduce biometrics' vulnerabilities in situations of unaware coercion. This new factor can be seen as a hybrid between knowledge and secret-based authentication in the form of a user-defined movement sequence performed by a body part relating to the biometric chosen in the system; assumed to increase both complexity and memorability compared to traditional user-defined passwords.

We propose a concept for such a two-factor continuous authentication system, including constant, periodic, and punctual verification of the user's identity, introducing time as an additional factor affecting the system's overall security. Vulnerabilities of the individual and combined factors are explored -- considering the probability of compromising the system and the duration any adversary can spend within such a system. This thesis contributes exploration and definitions within this space, threat modelling of the proposed system, and a simple prototype as a proof of concept.

## Class diagram
This class diagram illustrates the overall structure of the prototype, which is organised through the model-view-controller architectural pattern. It contains 15 classes, including three designed to follow the singleton pattern (`MovementClassifier`, `FaceClassifier`, `PersonsDB`), three object expressions that create objects of anonymous classes (`FaceMovement`, `BitmapUtils`, `ByteBufferUtils`), one data class that simply holds data (`Frame`) and one enumeration class that holds some constants (`LensFacing`). Additional three classes usually required to store data in the SQLite database -- built in on Android devices -- can be found in the code-base. As is customary in mobile applications, all behaviours that do not affect the user-interface thread run in parallel on background threads.
![Continuous_Authentication_class_diagram](https://user-images.githubusercontent.com/60703644/119847113-fe2ef300-bf0a-11eb-95f8-a5f07daf1762.png)
