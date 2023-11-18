# BAuction - Compilation Guide

## Prerequisites
Before compiling BAuction, ensure you have the following prerequisites installed on your system:
- JDK 16
- Maven
- BuildTools

## Getting BuildTools
1. Download [BuildTools](https://www.spigotmc.org/wiki/buildtools/).

2. Complete Spigot JAR for version 1.16.5 using the following command:
   ```bash
   java -jar BuildTools.jar --rev 1.16.5
   ```

## Compiling BAuction
1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com/By1337/BAuction.git
   ```

2. Navigate to the project directory:
   ```bash
   cd BAuction
   ```

3. Run the following Maven command to clean and install the project:
   ```bash
   mvn clean install
   ```

## Usage
After successfully compiling BAuction, you can use the generated JAR file in your Spigot server plugin directory.

## Contributing
Feel free to contribute to the development of BAuction by submitting issues or pull requests.

## License
This project is licensed under the [MIT License](LICENSE).

---