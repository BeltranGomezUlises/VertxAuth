-- MySQL dump 10.16  Distrib 10.1.26-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: auth
-- ------------------------------------------------------
-- Server version	10.1.26-MariaDB-0+deb9u1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `branchoffice`
--

DROP TABLE IF EXISTS `branchoffice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `branchoffice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branchoffice`
--

LOCK TABLES `branchoffice` WRITE;
/*!40000 ALTER TABLE `branchoffice` DISABLE KEYS */;
INSERT INTO `branchoffice` VALUES (1,'valle',1,'2018-08-07 01:52:41',1,NULL,NULL);
/*!40000 ALTER TABLE `branchoffice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(120) NOT NULL,
  `phone` char(10) DEFAULT NULL,
  `pass` varchar(255) NOT NULL,
  `blocked` tinyint(1) NOT NULL DEFAULT '0',
  `blocked_until_date` datetime DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_email` (`email`),
  UNIQUE KEY `unique_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'ulises','beltrangomezulises@gmail.com','6672118438','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4',0,'2018-08-07 01:27:31',1,'2018-08-07 01:27:31',1,NULL,NULL),(7,'Jaime Rosas','jarosas@allabordo.com','6672000000','c3c35aafaafd3ee318492df065041dbdcc532b88e6e82ef2c66ddbcf76316749',0,NULL,1,'2018-08-07 13:33:59',1,NULL,NULL),(8,'Vladimir Rubio Ochoa','vrubio@allabordo.com','6672000001','c2224727687415c9bb9487a37cc95db5fe5f7dd55550bcdf0434d14faed9b0ac',0,NULL,1,'2018-08-07 13:34:36',1,NULL,NULL),(9,'Ana Karen Lugo','alugo@allabordo.com','6672000002','9ac528105ffafd7e2856b5a0709f7b0881ee2287c2e94524b830683f0e2a8753',0,NULL,1,'2018-08-07 13:35:03',1,NULL,NULL),(10,'Jorge Guzman','jorge@indqtech.com','6672000003','6908b538e968cd68139ac10b34cb4950ed399a60e142633ba65dc10772fe7b5a',0,NULL,1,'2018-08-07 13:35:43',1,NULL,NULL),(11,'Nadia Rivera','nadia@indqtech.com','6672000004','6908b538e968cd68139ac10b34cb4950ed399a60e142633ba65dc10772fe7b5a',0,NULL,1,'2018-08-07 13:36:07',1,NULL,NULL),(12,'Hernán Robles','hrobles@allabordo.com','6672000005','9ac528105ffafd7e2856b5a0709f7b0881ee2287c2e94524b830683f0e2a8753',0,NULL,1,'2018-08-07 13:36:39',1,NULL,NULL),(13,'Isaias Galaviz Nafarrate','igalaviz@allabordo.com','6672000006','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4',0,NULL,1,'2018-08-07 13:37:40',1,NULL,NULL),(14,'Dalia Carlon','dalia@indqtech.com','6672000007','bc64c2afaaacb52547494ee9bdb9f9c43f58af56d8db781771718dfb677b0795',0,NULL,1,'2018-08-07 13:41:30',1,NULL,NULL),(16,'Gerardo Valdez Heredia','gvaldez@allabordo.com','6672000008','cc539efb246c30a26dfe3311bf13d276b0ff9ff492c3022013a2db9ae6c287b2',0,NULL,1,'2018-08-07 13:41:57',1,NULL,NULL);
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee_permission_branchoffice`
--

DROP TABLE IF EXISTS `employee_permission_branchoffice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employee_permission_branchoffice` (
  `employee_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  `branchoffice_id` int(11) NOT NULL,
  PRIMARY KEY (`employee_id`,`permission_id`,`branchoffice_id`),
  KEY `fk_permission_employee_branchoffice` (`permission_id`),
  KEY `fk_branchoffice_profile_employee` (`branchoffice_id`),
  CONSTRAINT `fk_branchoffice_profile_employee` FOREIGN KEY (`branchoffice_id`) REFERENCES `branchoffice` (`id`),
  CONSTRAINT `fk_employee_permission_branchoffice` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_permission_employee_branchoffice` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee_permission_branchoffice`
--

LOCK TABLES `employee_permission_branchoffice` WRITE;
/*!40000 ALTER TABLE `employee_permission_branchoffice` DISABLE KEYS */;
/*!40000 ALTER TABLE `employee_permission_branchoffice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `employee_profile`
--

DROP TABLE IF EXISTS `employee_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `employee_profile` (
  `employee_id` int(11) NOT NULL,
  `profile_id` int(11) NOT NULL,
  PRIMARY KEY (`employee_id`,`profile_id`),
  KEY `fk_profile_employee` (`profile_id`),
  CONSTRAINT `fk_employee_profile` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `fk_profile_employee` FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee_profile`
--

LOCK TABLES `employee_profile` WRITE;
/*!40000 ALTER TABLE `employee_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `employee_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `module`
--

DROP TABLE IF EXISTS `module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `module` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `module`
--

LOCK TABLES `module` WRITE;
/*!40000 ALTER TABLE `module` DISABLE KEYS */;
INSERT INTO `module` VALUES (1,'Sell',1,'2018-08-07 01:53:02',1,NULL,NULL),(2,'Configurations',1,'2018-08-07 01:53:08',1,NULL,NULL),(3,'Other',1,'2018-08-07 01:54:07',1,NULL,NULL);
/*!40000 ALTER TABLE `module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `sub_module_id` int(11) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_permission_sub_module` (`sub_module_id`),
  CONSTRAINT `fk_permission_sub_module` FOREIGN KEY (`sub_module_id`) REFERENCES `sub_module` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (1,'alta',1,1,'2018-08-07 01:55:52',1,NULL,NULL),(2,'registro',1,1,'2018-08-07 01:55:54',1,NULL,NULL),(3,'ver',1,1,'2018-08-07 01:55:56',1,NULL,NULL),(4,'cancelar',2,1,'2018-08-07 01:56:04',1,NULL,NULL),(5,'reportes',2,1,'2018-08-07 01:56:12',1,NULL,NULL),(6,'eliminar',2,1,'2018-08-07 01:56:16',1,NULL,NULL),(7,'accion ejemplo',3,1,'2018-08-07 01:56:22',1,NULL,NULL);
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile`
--

DROP TABLE IF EXISTS `profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile`
--

LOCK TABLES `profile` WRITE;
/*!40000 ALTER TABLE `profile` DISABLE KEYS */;
INSERT INTO `profile` VALUES (1,'admin','administrator of the system',1,'2018-08-07 01:52:08',1,NULL,NULL),(2,'reporter','read only operations',1,'2018-08-07 01:52:21',1,NULL,NULL);
/*!40000 ALTER TABLE `profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_permission_branchoffice`
--

DROP TABLE IF EXISTS `profile_permission_branchoffice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_permission_branchoffice` (
  `profile_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  `branchoffice_id` int(11) NOT NULL,
  PRIMARY KEY (`profile_id`,`permission_id`,`branchoffice_id`),
  KEY `fk_permission_profile_branchoffice` (`permission_id`),
  KEY `fk_branchoffice_profile_profile` (`branchoffice_id`),
  CONSTRAINT `fk_branchoffice_profile_profile` FOREIGN KEY (`branchoffice_id`) REFERENCES `branchoffice` (`id`),
  CONSTRAINT `fk_permission_profile_branchoffice` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`),
  CONSTRAINT `fk_profile_permission_branchoffice` FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_permission_branchoffice`
--

LOCK TABLES `profile_permission_branchoffice` WRITE;
/*!40000 ALTER TABLE `profile_permission_branchoffice` DISABLE KEYS */;
/*!40000 ALTER TABLE `profile_permission_branchoffice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sub_module`
--

DROP TABLE IF EXISTS `sub_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sub_module` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `module_id` int(11) NOT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int(11) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_sub_module_module` (`module_id`),
  CONSTRAINT `fk_sub_module_module` FOREIGN KEY (`module_id`) REFERENCES `module` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sub_module`
--

LOCK TABLES `sub_module` WRITE;
/*!40000 ALTER TABLE `sub_module` DISABLE KEYS */;
INSERT INTO `sub_module` VALUES (1,'Normal',1,1,'2018-08-07 01:54:38',1,NULL,NULL),(2,'Special',1,1,'2018-08-07 01:54:49',1,NULL,NULL),(3,'Other',2,1,'2018-08-07 01:54:54',1,NULL,NULL),(4,'Principal',2,1,'2018-08-07 01:54:59',1,NULL,NULL),(5,'example',3,1,'2018-08-07 01:55:10',1,NULL,NULL);
/*!40000 ALTER TABLE `sub_module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'auth'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-08-07 14:03:17
