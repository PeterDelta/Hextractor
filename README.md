# Hextractor pre-release 1.60

Fork de la herramienta creada por Wave para traducir juegos clásicos como: Nes, Super Nintendo, Mega Drive, Master System, Game Boy, etc.

[github.com/sewave/hextractor](https://github.com/sewave/hextractor)

[Guia](https://traduccioneswave.blogspot.com/p/traducir-juegos-con-hextractor.html)

## ✨ Mejoras y Optimizaciones

 - Actualizado a Java 21
 - Soporte DPI completo para pantallas 4K y alta resolución
 - Añadidos idiomas: Inglés, Español, Francés, Alemán, Italiano, Portugués, Ruso
 - Nueva función de Comparar ROMs. Extrae el contenido modificado a un .ext para tenerlo todo mas ordenado. (Para insertarlos automáticamente, todos los .ext deben empezar por "TR_") Útil para tener a parte los gráficos modificados por ejemplo.
 - Añadido soporte multi-byte. Establecer en la tabla entre {} lo que quieras mostrar de la cadena hexadecimal, ejemplo: 020F00={fin}
 - Añadido soporte para varias tablas. Su nombre debe terminar con el sufijo que se quiera despues de _ Ejemplo: "Sonic_1.tbl" "Sonic_1.ext". Así queda asociada esa tabla a ese script.
 - Añadido soporte para punteros: Selecciona automáticamente la zona establecida entre el inicio y final del offset y marca todos los que haya en la rom o en una zona determinada.

## 📋 Requisitos

- **Java 21 o superior** [web oficial](https://www.oracle.com/java/technologies/downloads/#java21)


## 📜 Créditos

- **Código original**: Wave ([github.com/sewave/hextractor](https://github.com/sewave/hextractor))
- **Optimizaciones y modernización**: PeterDelta (2025)

## 📄 Licencia

Mismo modelo de licencia que el proyecto original.

______________________________________________________________

# Hextractor pre-release 1.60

Fork of the tool created by Wave to translate classic games  like: Nes, Super Nintendo, Mega Drive, Master System, Game Boy, etc.:
[github.com/sewave/hextractor](https://github.com/sewave/hextractor)

[Guide](https://traduccioneswave.blogspot.com/p/traducir-juegos-con-hextractor.html)

## ✨ Improvements and Optimizations

 - Updated to Java 21
 - Full DPI support for 4K and high-resolution screens
 - Added languages, supports: English, Spanish, French, German, Italian, Portuguese, Russian
 - New ROM Comparison function. Extracts modified content to a .ext file to keep everything more organized. (To automatically insert all .ext files, they must start with "TR_") Useful to keep modified graphics separate, for example.
 - - Added multi-byte support. Specify the value of the hexadecimal string you want to display within curly braces {}, for example: 020F00={end}
 - Added support for multiple tables. Their names must end with the desired suffix after _. Example: "Sonic_1.tbl" "Sonic_1.ext". This associates the table with the script.
- Added pointer support: Automatically selects the area set between the start and end of the offset and marks all those in the ROM or in a given area.

## 📋 Requirements

- **Java 21 or higher** [official website](https://www.oracle.com/java/technologies/downloads/#java21)


## 📜 Credits

- **Original code**: Wave ([github.com/sewave/hextractor](https://github.com/sewave/hextractor))
- **Optimizations and modernization**: PeterDelta (2025)

## 📄 License

Same license model as the original project.