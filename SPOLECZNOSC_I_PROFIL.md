# Specyfikacja Modułu: Społeczność i Profil

## Wersja: 1.7 (stan na 2024-08-01)

---

## 1. Cel Główny

Celem tego modułu jest stworzenie w pełni funkcjonalnego systemu społecznościowego, który umożliwi użytkownikom interakcję, budowanie sieci kontaktów (znajomych) oraz zarządzanie swoim publicznym profilem. Moduł ten jest kluczowy dla budowania zaangażowania i umożliwienia rywalizacji między graczami.

---

## 2. Model Danych (User)

Centralnym modelem danych dla tego modułu jest klasa `User`. Będzie ona przechowywać wszystkie informacje o profilu i relacjach społecznościowych użytkownika.

**Definicja `data class User`:**
*   `uid: String` - (Klucz główny) Unikalny ID z Firebase Authentication.
*   `username: String` - **(Obowiązkowe)** Publiczna, unikalna nazwa wyświetlana.
*   `username_lowercase: String` - Wersja `username` z małymi literami, do wyszukiwania.
*   `email: String` - Adres e-mail (niepubliczny).
*   `firstName: String` - **(Obowiązkowe)** Imię użytkownika.
*   `firstName_lowercase: String` - Wersja `firstName` z małymi literami, do wyszukiwania.
*   `lastName: String` - **(Obowiązkowe)** Nazwisko użytkownika.
*   `lastName_lowercase: String` - Wersja `lastName` z małymi literami, do wyszukiwania.
*   `publicProfile: Boolean` - Flaga prywatności. Jeśli `true`, profil jest publiczny. Domyślnie `true`.
*   `club: String?` - **(Opcjonalne)** Nazwa klubu, do którego należy gracz.
*   `profileImageUrl: String?` - **(Opcjonalne)** URL do zdjęcia profilowego.
*   `friends: List<String>` - Lista `uid` użytkowników, którzy są znajomymi.
*   `friendRequestsSent: List<String>` - Lista `uid` użytkowników, do których wysłano zaproszenie.
*   `friendRequestsReceived: List<String>` - Lista `uid` użytkowników, od których otrzymano zaproszenie.

---

## 3. Plan Implementacji Funkcjonalności

### Krok 1: Onboarding - Uzupełnienie Profilu po Rejestracji

**Cel:** Zapewnienie, że każdy użytkownik ma kompletny profil podstawowy.

1.  **Wykrywanie Nowego Użytkownika:**
    *   Po pomyślnym zalogowaniu, aplikacja (w `AuthViewModel`) pobierze dane użytkownika z Firestore.
    *   Sprawdzi, czy pole `username` jest puste.
2.  **Przekierowanie do Formularza:**
    *   Jeśli `username` jest pusty, użytkownik zostanie przekierowany do nowego ekranu `SetupProfileScreen`.
    *   Jeśli `username` nie jest pusty, użytkownik zostanie przekierowany do głównego ekranu aplikacji (`MainScreen`).
3.  **Ekran `SetupProfileScreen`:**
    *   Będzie zawierał formularz z polami: `username`, `firstName`, `lastName` - **wszystkie pola są obowiązkowe**.
    *   **Walidacja:** Pole `username` nie może zawierać spacji i musi być unikalne w bazie. Walidacja odbywa się w czasie rzeczywistym, a przycisk zapisu jest nieaktywny, dopóki wszystkie pola nie są poprawnie wypełnione.
    *   Zawiera przełącznik `Switch` do ustawienia profilu jako publiczny/prywatny.
4.  **Logika Zapisu:**
    *   Po kliknięciu przycisku, dane zostaną zwalidowane (czy `username` nie jest pusty i czy jest unikalny).
    *   Aplikacja zaktualizuje dokument użytkownika w Firestore o nowe dane, w tym o wersje `_lowercase` dla pól tekstowych.
    *   Po pomyślnym zapisie, użytkownik zostanie przekierowany do `MainScreen`.

### Krok 2: Kontekstowe Wymaganie Danych (Turnieje i Kluby)

**Cel:** Wdrożenie strategii "pytaj tylko, gdy to konieczne".

*   **Implementacja:** W przyszłości, podczas implementacji modułu turniejów lub klubów, przed dołączeniem do turnieju lub klubu, aplikacja sprawdzi, czy pola `firstName` i `lastName` w profilu użytkownika są uzupełnione.
*   **Dialog:** Jeśli pola te są puste, zostanie wyświetlony dialog z prośbą o ich uzupełnienie, informując użytkownika, że są one wymagane do udziału w oficjalnych rozgrywkach.

### Krok 3: Ekran Społeczności (`CommunityScreen`)

**Cel:** Stworzenie centralnego miejsca do zarządzania interakcjami społecznymi.

Zaimplementowano strukturę z trzema zakładkami: "Szukaj", "Znajomi", "Zaproszenia".

1.  **Wyszukiwarka Graczy (zakładka "Szukaj"):**
    *   Pole tekstowe do wpisywania frazy.
    *   **Wyszukiwanie odbywa się "na żywo"** (z 500ms opóźnieniem) po wpisaniu co najmniej 3 znaków.
    *   **Automatyczne odświeżanie:** Wyniki wyszukiwania są automatycznie odświeżane po każdym powrocie do zakładki, aby zapewnić aktualność statusów relacji. Zastosowano tu wzorzec **"Ręcznego Odświeżania Stanu po Akcji"** (szczegóły w `PODSUMOWANIE_POWIADOMIEN_FCM.md`).
    *   Wyniki wyświetlane są w `LazyColumn` jako estetyczne karty (`UserCard`).
    *   Kliknięcie na wynik nawiguje do ekranu profilu danego użytkownika (`UserProfileScreen`).
2.  **Zarządzanie Zaproszeniami (zakładka "Zaproszenia"):**
    *   **Podział na widoki:** Ekran posiada dwie pod-zakładki: "Otrzymane" i "Wysłane".
    *   **Akcje:** Użytkownik może akceptować/odrzucać otrzymane zaproszenia oraz anulować wysłane. Po każdej z tych akcji lista zaproszeń (i wyników wyszukiwania) odświeża się automatycznie dzięki wzorcowi **"Ręcznego Odświeżania Stanu po Akcji"**.
    *   **Interaktywność:** Karty z zaproszeniami są teraz klikalne (poza przyciskami akcji), co pozwala na bezpośrednie przejście do profilu osoby zapraszającej.
3.  **Lista Znajomych (zakładka "Znajomi"):**
    *   Sekcja wyświetla listę aktualnych znajomych użytkownika.
    *   **Spójny wygląd:** Wygląd listy jest ujednolicony z wynikami wyszukiwania dzięki zastosowaniu reużywalnego komponentu `UserCard`.
    *   **Usuwanie znajomych:** Każda karta znajomego posiada przycisk do usunięcia go z listy, zabezpieczony dialogiem potwierdzającym. Lista odświeża się automatycznie.
    *   Kliknięcie w kartę prowadzi do profilu gracza.

### Krok 4: Implementacja Logiki Systemu Znajomych (`CommunityRepository`)

**Cel:** Stworzenie logiki backendowej do zarządzania relacjami.

1.  **Wysyłanie Zaproszenia (`sendFriendRequest`):** Aktualizuje pola `friendRequestsSent` u wysyłającego i `friendRequestsReceived` u odbiorcy.
2.  **Akceptowanie Zaproszenia (`acceptFriendRequest`):** Dodaje ID obu użytkowników do ich wzajemnych list `friends` i usuwa wpisy z list z zaproszeniami.
3.  **Odrzucanie Zaproszenia (`rejectFriendRequest`):** Usuwa wpisy z list z zaproszeniami.
4.  **Usuwanie Znajomego (`removeFriend`):** Usuwa ID obu użytkowników z ich wzajemnych list `friends`.

### Krok 5: Ekran Profilu Użytkownika (`UserProfileScreen`)

**Cel:** Stworzenie publicznej wizytówki gracza oraz miejsca do edycji własnych danych.

**Przebudowa UI i Logiki (stan na 2024-08-01):**

Ekran profilu został gruntownie przebudowany w celu uproszczenia wyglądu i dodania nowych funkcji.
*   **Layout:** Usunięto `TopAppBar` na rzecz "lekkiego" przycisku "Cofnij" (szewron z tekstem), umieszczonego bezpośrednio na ekranie dla zachowania spójności z innymi ekranami drugiego poziomu.
*   **Dynamiczne przyciski:** Centralnym elementem są dynamiczne przyciski akcji, które zmieniają się w zależności od relacji z oglądanym użytkownikiem (np. "Dodaj do znajomych", "Akceptuj/Odrzuć", "Usuń znajomego", "Zarządzaj profilem"). Cała logika jest zaimplementowana i działa z poziomu `ProfileViewModel`.
*   **Logika Widoczności:** Doprecyzowano logikę widoczności. Główna treść profilu jest dostępna, jeśli profil jest publiczny, użytkownik jest znajomym lub ogląda własny profil. W przeciwnym razie wyświetlany jest ekran profilu prywatnego.
    *   **Karta 'Statystyki'** jest widoczna tylko, gdy profil jest publiczny, gdy przeglądający jest znajomym, lub gdy użytkownik ogląda własny profil (nawet jeśli jest on prywatny).
*   **Nowe Funkcje:**
    *   Usunięto zbędną kartę "Informacje".
    *   Wprowadzono nową kartę **"Interakcje"** z przyciskiem **"Rozpocznij mecz"**, widoczną na profilach wszystkich innych użytkowników.
    *   Ekran dla profili prywatnych (`PrivateProfileContent`) również wyświetla teraz dynamiczne przyciski akcji (np. 'Akceptuj', 'Anuluj zaproszenie') oraz opcję wysłania wiadomości.

### Krok 6: Dedykowany Ekran Zarządzania Profilem (`ManageProfileScreen`)

*   Zrezygnowano z prostego dialogu na rzecz dedykowanego ekranu dostępnego po kliknięciu "Zarządzaj profilem" na własnym profilu.
*   Ekran posiada spójny z `UserProfileScreen` przycisk "Cofnij".
*   Pierwszą zaimplementowaną opcją jest możliwość zmiany statusu profilu (publiczny/prywatny) za pomocą przełącznika `Switch`. Zmiana jest zapisywana w bazie danych w czasie rzeczywistym i od razu widoczna w UI.

---

## 4. Wygląd i Interfejs Użytkownika (UI/UX) Modułu Społeczność

### 4.1. Ekran Społeczność - Styl Menu Zakładek
*   Ekran `CommunityScreen` wykorzystuje górny pasek zakładek (`TabRow`) do organizacji treści.
*   **Zakładki:** Trzy główne zakładki: "Szukaj", "Znajomi", "Zaproszenia".

### 4.2. Reużywalny Komponent `UserCard`
*   **Cel:** Zapewnienie spójnego wyglądu dla każdego elementu listy przedstawiającego użytkownika.
*   **Implementacja:** Stworzono komponent `@Composable fun UserCard`, który został wydzielony do osobnego pliku (`ui/screens/common/UserCard.kt`).
*   **Dynamiczne Akcje:** Komponent dynamicznie dostosowuje wyświetlane ikony akcji (dodaj, usuń, czat, zaproszenie wysłane) na podstawie przekazanego statusu relacji (`RelationshipStatus`).
*   **Zastosowanie:** Używany zarówno w wynikach wyszukiwania, jak i na liście znajomych.

### 4.3. Potwierdzenia Akcji (Snackbar i Dialog)
*   **Snackbar:** Wszystkie szybkie akcje (wysłanie, akceptacja, odrzucenie zaproszenia) są potwierdzane przez globalny `SnackbarManager`.
*   **AlertDialog:** Akcje destrukcyjne, takie takie jak usunięcie znajomego, są dodatkowo zabezpieczone przez dialog z prośbą o potwierdzenie, aby zapobiec przypadkowym działaniom.

### 4.4. Ulepszenia Zakładki "Zaproszenia" (2025-11-09)

- **Problem:** Przyciski akcji (akceptuj/odrzuć) w zaproszeniach były stylistycznie niespójne (jeden pełny, drugi obramowany) i miały mało intuicyjne ikony.
- **Rozwiązanie:** Zastąpiono poprzednie ikony bardziej czytelnymi z biblioteki Material Design Icons:
  - Akceptuj: `Icons.Default.PersonAdd` (ikona osoby z plusem), w zielonym kolorze tła.
  - Odrzuć: `Icons.Default.PersonRemove` (ikona osoby z minusem), w czerwonym kolorze tła.
- **Interaktywność Kart Zaproszeń:** Cała karta zaproszenia jest teraz klikalna. Kliknięcie w kartę (poza przyciskami akcji) nawiguje do ekranu profilu użytkownika, który wysłał zaproszenie (`user_profile/{userId}`).

---

## 5. Diagnostyka i Naprawy (Listopad 2025)

### 5.1. Ujednolicenie Obsługi Stanu (`Resource` vs `Response`)
- **Problem:** W projekcie istniały dwie oddzielne klasy (`Resource` i `Response`) służące do tego samego celu - opakowywania wyników operacji asynchronicznych. Powodowało to konflikty typów i awarie aplikacji, gdy różne moduły (np. Czat i Społeczność) musiały ze sobą współpracować.
- **Rozwiązanie:** Przeprowadzono refaktoryzację całej aplikacji. Stara klasa `Response` została usunięta, a wszystkie repozytoria, ViewModele i ekrany UI zostały zaktualizowane, aby używać wyłącznie nowej, spójnej klasy `sealed class Resource<out T>`.

### 5.2. Poprawki Reguł Bezpieczeństwa Firebase
- **Problem:** System znajomych (akceptowanie/odrzucanie zaproszeń) oraz wyszukiwarka graczy przestały działać, zwracając błąd `PERMISSION_DENIED`.
- **Przyczyna:** Wprowadzone wcześniej reguły bezpieczeństwa dla modułu czatu były zbyt restrykcyjne i przypadkowo zablokowały niezbędne operacje na kolekcji `users`. Reguły zezwalały użytkownikowi na edycję tylko i wyłącznie własnego dokumentu, podczas gdy system znajomych wymaga aktualizacji dokumentów obu użytkowników.
- **Ostateczne Rozwiązanie:** Zaktualizowano reguły Firestore, aby były bardziej elastyczne. Nowa reguła `allow update: if request.auth != null;` dla ścieżki `/users/{userId}` pozwala każdemu zalogowanemu użytkownikowi na aktualizację dowolnego profilu. Jest to kompromis między bezpieczeństwem a funkcjonalnością po stronie klienta. W przyszłości, dla zwiększenia bezpieczeństwa, ta logika powinna zostać przeniesiona do Cloud Functions.

### 5.3. Poprawki Interfejsu Użytkownika w "Zaproszeniach"
- **Problem:** W zakładce "Zaproszenia", pod-menu do filtrowania ("Otrzymane" / "Wysłane") zostało błędnie zaimplementowane jako drugi `TabRow` pod głównym, co wyglądało źle i było nieintuicyjne.
- **Rozwiązanie:** Przywrócono poprawny wygląd. Wewnętrzne menu zostało zaimplementowane przy użyciu komponentu `FilterChip`, co tworzy wizualnie lżejszy i bardziej czytelny interfejs w formie dwóch przycisków-pigułek.
