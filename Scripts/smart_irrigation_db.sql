-- Progettazione IOT 
DROP DATABASE if exists smart_irrigation_db_nuovo; 
CREATE DATABASE smart_irrigation_db_nuovo; 
USE smart_irrigation_db_nuovo; 
-- MySQL dump 10.13  Distrib 5.6.20, for Win32 (x86)
--
-- Host: localhost    Database: smart_irrigation_db_nuovo
-- ------------------------------------------------------
-- Server version	5.6.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ActuatorActivation`
--

DROP TABLE IF EXISTS `ActuatorActivation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ActuatorActivation` (
  `ActuatorId` char(30) NOT NULL,
  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Mode` char(30) DEFAULT NULL,
  PRIMARY KEY (`ActuatorId`,`Timestamp`),
  KEY `ActAssing` (`ActuatorId`),
  CONSTRAINT `ActAssing` FOREIGN KEY (`ActuatorId`) REFERENCES `ActuatorDevice` (`ActuatorId`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ActuatorActivation`
--

LOCK TABLES `ActuatorActivation` WRITE;
/*!40000 ALTER TABLE `ActuatorActivation` DISABLE KEYS */;
/*!40000 ALTER TABLE `ActuatorActivation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ActuatorDevice`
--

DROP TABLE IF EXISTS `ActuatorDevice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ActuatorDevice` (
  `ActuatorId` char(30) NOT NULL,
  `ZoneId` varchar(45) DEFAULT NULL,
  `IPaddress` varchar(45) DEFAULT NULL,
  `State` int(11) NOT NULL,
  `GeoCoordinates` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`ActuatorId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ActuatorDevice`
--

LOCK TABLES `ActuatorDevice` WRITE;
/*!40000 ALTER TABLE `ActuatorDevice` DISABLE KEYS */;
INSERT INTO `ActuatorDevice` VALUES ('Act1','Zone1','fd00::212:4b00:f82:8a06',0,'(1;1)'),('Act2','Zone2','fd00::212:4b00:f82:bf06',0,'(2;2)');
/*!40000 ALTER TABLE `ActuatorDevice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SensorDevice`
--

DROP TABLE IF EXISTS `SensorDevice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SensorDevice` (
  `SensorId` char(30) NOT NULL,
  `PairedAct` char(30) DEFAULT NULL,
  `ZoneId` varchar(45) DEFAULT NULL,
  `IPaddress` varchar(45) DEFAULT NULL,
  `GeoCoordinates` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`SensorId`),
  KEY `ActAssign` (`PairedAct`),
  CONSTRAINT `ActAssign` FOREIGN KEY (`PairedAct`) REFERENCES `ActuatorDevice` (`ActuatorId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SensorDevice`
--

LOCK TABLES `SensorDevice` WRITE;
/*!40000 ALTER TABLE `SensorDevice` DISABLE KEYS */;
INSERT INTO `SensorDevice` VALUES ('Hum2','Act2','Zone2','fd00::212:4b00:f82:104a','(22;22)'),('Tem2','Act2','Zone2','fd00::212:4b00:f82:3b12','(21;21)'),('TempHum1','Act1','Zone1','fd00::212:4b00:f82:15f1','(11;11)');
/*!40000 ALTER TABLE `SensorDevice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Temperature`
--

DROP TABLE IF EXISTS `Temperature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Temperature` (
  `SensorId` char(30) NOT NULL,
  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Value_C`  decimal(4,2) NOT NULL,
  PRIMARY KEY (`SensorId`,`Timestamp`),
  KEY `SensAssign` (`SensorId`),
  CONSTRAINT `SensAssignTemp` FOREIGN KEY (`SensorId`) REFERENCES `SensorDevice` (`SensorId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Temperature`
--

LOCK TABLES `Temperature` WRITE;
/*!40000 ALTER TABLE `Temperature` DISABLE KEYS */;
/*!40000 ALTER TABLE `Temperature` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `VolumetricWaterContent`
--

DROP TABLE IF EXISTS `VolumetricWaterContent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `VolumetricWaterContent` (
  `SensorId` char(30) NOT NULL,
  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Value_perc` decimal(4,2) NOT NULL,
  PRIMARY KEY (`SensorId`,`Timestamp`),
  KEY `SensAssign` (`SensorId`),
  CONSTRAINT `SensAssignVMC` FOREIGN KEY (`SensorId`) REFERENCES `SensorDevice` (`SensorId`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `VolumetricWaterContent`
--

LOCK TABLES `VolumetricWaterContent` WRITE;
/*!40000 ALTER TABLE `VolumetricWaterContent` DISABLE KEYS */;
/*!40000 ALTER TABLE `VolumetricWaterContent` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-09-10 20:56:18
