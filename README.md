# ParallelGroupDownloader

Pełna nazwa projektu:

Narzędzie do szybkiego kopiowania plików w obrębie sali komputerowej

## Podstawowe wymagania

1. Java bez dodatkowych zależności/bibliotek
2. Autokonfiguracja.
3. Dobór parametrów pracy na podstawie konfiguracji systemu np. ilość wolnej pamięci RAM.
4. Automatyczne wykrywanie, które z maszyn nie mogą skopiować pliku np. z powodu braku miejsca na dysku.
5. Maksymalna odporność na awarie.
6. Jasny stan po zakończeniu pracy. Albo się udało, albo nie i nie ma śladu po wykonywanych operacjach (zwolnienie miejsca na dysku).
7. Obsługa Unix-owego sygnalizowania powodzenia programu exit 0 - OK, exit zwraca coś innego oznacza błąd.
8. Plik musi zostać przekazany bezbłędnie.
9. Konfiguracja poprzez CLI
10. Opcjonalne (można włączyć lub nie) raportowanie w trakcie pracy programu postępu kopiowania i czasu do zakończenia.
11. I najważniejsze: szybkość działania.


## Idea
Chodzi o stworzenie aplikacji w Java, która będzie kopiować duży plik pomiędzy maszynami na jednej sali komputerowej. Źródłem pliku jest jedna z maszyn na sali lub serwer(y) WWW. Wszystkie maszyny na sali komputerowej są w tej samej sieci IP. Wiele z nich zostanie uruchomionych, lecz kolejność uruchomień może być dowolna. I tu istotna jest autokonfiguracja. Chodzi o to, aby automatycznie rozpoznać, które z maszyn działają (uruchomiono na nich aplikacje). Program musi także opóźnić uruchomieni transferu dając szanse na dołączenie kolejnym maszynom. 

Aby nie odkrywać koła na nowo, warto zerknąć na gotowe rozwiązania do transferu 1 do wielu poprzez UDP i broadcast. 
Jest taki zestaw programów: udp-sender / udp-receiver
Tam są możliwości konfiguracji, kiedy transfer się faktycznie rozpocznie.
