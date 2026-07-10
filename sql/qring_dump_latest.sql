-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: localhost    Database: qring_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `achievement_comment`
--

DROP TABLE IF EXISTS `achievement_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `achievement_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `min_rate` int NOT NULL,
  `max_rate` int NOT NULL,
  `comment_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `achievement_comment`
--

LOCK TABLES `achievement_comment` WRITE;
/*!40000 ALTER TABLE `achievement_comment` DISABLE KEYS */;
INSERT INTO `achievement_comment` VALUES (1,0,9,'이제 시작이에요! 첫걸음을 떼어볼까요?'),(2,10,30,'더디네요 좀만 더 노력해보세요!'),(3,31,50,'좋은 흐름이에요! 절반을 향해 달리는 중'),(4,51,70,'와우, 벌써 이만큼이나 선전하셨군요! 대단해요'),(5,71,99,'이번 주 엄청난 성장을 보여주고 있어요! 이대로 쭉 가보자고요'),(6,100,100,'완벽합니다! 목표를 마스터하셨어요');
/*!40000 ALTER TABLE `achievement_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapter`
--

DROP TABLE IF EXISTS `chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapter` (
  `chapter_id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_num` int DEFAULT NULL,
  `chapter_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `required_points` int DEFAULT NULL,
  `content_id` bigint NOT NULL,
  PRIMARY KEY (`chapter_id`),
  KEY `FK80tj1ir66aa8pi7xbqdu1pxq4` (`content_id`),
  CONSTRAINT `FK80tj1ir66aa8pi7xbqdu1pxq4` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapter`
--

LOCK TABLES `chapter` WRITE;
/*!40000 ALTER TABLE `chapter` DISABLE KEYS */;
INSERT INTO `chapter` VALUES (1,1,'도서관 좌석번호 64번',0,1),(2,1,'3번 관람석',0,2),(3,1,'끊어진 전화선',0,3),(4,1,'빨간 구두',0,4),(5,1,'남자친구가 제 꿈을 예측해요',0,5),(6,1,'수학 과외 선생님과 내 여자친구',0,6);
/*!40000 ALTER TABLE `chapter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content`
--

DROP TABLE IF EXISTS `content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content` (
  `content_id` bigint NOT NULL AUTO_INCREMENT,
  `status` varchar(20) DEFAULT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `required_points` int DEFAULT '0',
  `total_quiz_count` int DEFAULT '0',
  `total_chapters` int DEFAULT NULL,
  `level_code` int DEFAULT NULL,
  PRIMARY KEY (`content_id`),
  KEY `FKiudotpe4xsqlggmeaxf52r1rg` (`category_id`),
  KEY `FKc6mnk27ohvqcsjprwtx7i2g63` (`level_code`),
  CONSTRAINT `FKc6mnk27ohvqcsjprwtx7i2g63` FOREIGN KEY (`level_code`) REFERENCES `difficulty_level` (`level_code`),
  CONSTRAINT `FKiudotpe4xsqlggmeaxf52r1rg` FOREIGN KEY (`category_id`) REFERENCES `content_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content`
--

LOCK TABLES `content` WRITE;
/*!40000 ALTER TABLE `content` DISABLE KEYS */;
INSERT INTO `content` VALUES (1,'ACTIVE','/images/짝사랑_썸네일.png','도서관 좌석번호 64번',1,0,0,NULL,NULL),(2,'ACTIVE','/images/드라마_썸네일.png','3번 관람석',1,0,0,NULL,NULL),(3,'ACTIVE','/images/스릴러_썸네일.png','끊어진 전화선',2,0,0,NULL,NULL),(4,'ACTIVE','/images/추리물_썸네일.png','빨간 구두',2,0,0,NULL,NULL),(5,'ACTIVE','/images/특이한연애썰_썸네일.png','남자친구가 제 꿈을 예측해요',1,0,0,NULL,NULL),(6,'ACTIVE','/images/연애갈등_썸네일.png','수학 과외 선생님과 내 여자친구',2,0,0,NULL,NULL);
/*!40000 ALTER TABLE `content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_category`
--

DROP TABLE IF EXISTS `content_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL,
  `display_order` int DEFAULT NULL,
  `story_count` int DEFAULT '0',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_category`
--

LOCK TABLES `content_category` WRITE;
/*!40000 ALTER TABLE `content_category` DISABLE KEYS */;
INSERT INTO `content_category` VALUES (1,'로맨스',1,3),(2,'스토리',2,3),(3,'스릴러',3,0),(4,'추리물',4,0),(5,'특이한연애썰',5,0),(6,'연애갈등',6,0);
/*!40000 ALTER TABLE `content_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `daily_goal_record`
--

DROP TABLE IF EXISTS `daily_goal_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_goal_record` (
  `goal_record_id` bigint NOT NULL AUTO_INCREMENT,
  `achieve_date` date DEFAULT NULL,
  `is_attained` bit(1) DEFAULT NULL,
  `completed_chapter_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`goal_record_id`),
  KEY `FKjwdwgsgfsym6yb1gy8sasn0of` (`completed_chapter_id`),
  KEY `FKl9pqbfiq9mjcdd9utaijf8v2a` (`user_id`),
  CONSTRAINT `FKjwdwgsgfsym6yb1gy8sasn0of` FOREIGN KEY (`completed_chapter_id`) REFERENCES `chapter` (`chapter_id`),
  CONSTRAINT `FKl9pqbfiq9mjcdd9utaijf8v2a` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `daily_goal_record`
--

LOCK TABLES `daily_goal_record` WRITE;
/*!40000 ALTER TABLE `daily_goal_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `daily_goal_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `difficulty_level`
--

DROP TABLE IF EXISTS `difficulty_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `difficulty_level` (
  `level_code` int NOT NULL,
  `level_desc` varchar(255) DEFAULT NULL,
  `level_name` varchar(20) NOT NULL,
  PRIMARY KEY (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `difficulty_level`
--

LOCK TABLES `difficulty_level` WRITE;
/*!40000 ALTER TABLE `difficulty_level` DISABLE KEYS */;
INSERT INTO `difficulty_level` VALUES (1,'기초 영어 단어 수준','쉬움'),(2,'문장 완성 수준','보통'),(3,'문맥 이해 수준','어려움');
/*!40000 ALTER TABLE `difficulty_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_detail`
--

DROP TABLE IF EXISTS `quiz_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_detail` (
  `quiz_id` bigint NOT NULL AUTO_INCREMENT,
  `correct_answer` text,
  `explanation` text,
  `quiz_type` varchar(200) DEFAULT NULL,
  `content_id` bigint NOT NULL,
  `script_id` bigint NOT NULL,
  `question` text,
  `options` json DEFAULT NULL,
  `difficulty` int DEFAULT NULL,
  `acceptable_answers` json DEFAULT NULL,
  `hint` text,
  PRIMARY KEY (`quiz_id`),
  KEY `FKhpjjt22h2je6erxuxsdfqbptw` (`content_id`),
  KEY `FKmj5hwc5l6f7k9s69gnuxr6mpp` (`script_id`),
  CONSTRAINT `FKhpjjt22h2je6erxuxsdfqbptw` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKmj5hwc5l6f7k9s69gnuxr6mpp` FOREIGN KEY (`script_id`) REFERENCES `script` (`script_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_detail`
--

LOCK TABLES `quiz_detail` WRITE;
/*!40000 ALTER TABLE `quiz_detail` DISABLE KEYS */;
INSERT INTO `quiz_detail` VALUES (1,'annoyed','\'annoyed\'는 뭔가 때문에 짜증나거나 화날 때 쓰는 표현이야. 자리 문제로 짜증난 상황이지.','multiple_choice',1,11,'다음 중 \'짜증나는, 화나는\'의 영어 표현으로 알맞은 것은?','[\"annoyed\", \"awkward\", \"anxious\", \"amazing\"]',1,NULL,'annoy(짜증나게 하다)에 -ed, 모기 앵앵거릴 때 느끼는 그 감정 🦟'),(2,'slyly','\'slyly glance\'는 몰래 살짝 쳐다본다는 뜻이야. 상황을 파악하려고 조심스럽게 볼 때 쓰는 표현이지.','subjective',1,11,'\'살짝/몰래\' = s______\n\nI ______ glanced at her while she was reading.\n(뜻: 그녀가 책을 읽는 동안 나는 살짝 쳐다봤다)',NULL,2,'[\"slyly\", \"Slyly\", \"sly\"]','s로 시작하는 5글자, \'미끄러지다\'는 뜻도 있어 🕵️'),(3,'awkward','\'awkward\'는 어색하고 불편한 상황을 표현할 때 쓰는 단어야. uncomfortable, weird도 비슷한 의미로 쓸 수 있어.','subjective',1,11,'\'말 걸기 애매했다\'\n\nShe was so focused on her book that it felt ______ to strike up a conversation.\n(뜻: 어색한, 민망한)',NULL,3,'[\"awkward\", \"uncomfortable\", \"weird\", \"strange\"]','a_____ (어색하고 불편한 상황을 표현, 7글자)'),(4,'curiosity','\'curiosity\'는 궁금해하는 마음, 호기심을 뜻해. 쪽지 때문에 호기심이 생긴 상황이지.','multiple_choice',1,22,'다음 중 \'호기심\'을 뜻하는 영어 단어는?','[\"confusion\", \"creativity\", \"confidence\", \"curiosity\"]',1,NULL,'Curiosity killed the cat(호기심이 고양이를 죽였다) 🐱 그 단어!'),(5,'curiosity','\'curiosity\'는 호기심이라는 뜻이야. \'curiosity killed the cat\'이라는 유명한 속담도 있어.','subjective',1,22,'\'호기심\' = c______\n\nMy ______ about the mysterious notes kept growing.\n(뜻: 그 신비한 쪽지들에 대한 호기심이 계속 커졌다)',NULL,2,'[\"curiosity\", \"Curiosity\"]','c로 시작하는 9글자 단어, 고양이를 죽인다는 유명한 속담의 주인공 🐱'),(6,'on purpose','\'on purpose\'는 일부러, 의도적으로 한다는 뜻이야. intentionally, deliberately도 같은 의미로 쓸 수 있어.','subjective',1,22,'\'일부러 일찍 가봤다\'\n\nI went there ______ ______ out of curiosity.\n(뜻: 의도적으로, 일부러)',NULL,3,'[\"on purpose\", \"intentionally\", \"deliberately\"]','on p_______ (2단어, \'목적\'이란 뜻의 단어)'),(7,'courage','\'courage\'는 용기를 뜻해. 무서워도 해내는 담력을 말하지.','multiple_choice',1,33,'다음 중 \'용기, 담력\'을 뜻하는 영어 단어는?','[\"careful\", \"courage\", \"comfort\", \"curious\"]',1,NULL,'encourage(격려하다)에서 en을 빼 보면 💪'),(8,'exchanging','\'exchange\'는 서로 주고받다라는 뜻으로, 쪽지를 주고받는 상황에 딱 맞는 표현이야.','fill_in_blank',1,33,'From that day on, we started ______ notes back and forth.','[\"sharing\", \"writing\", \"sending\", \"exchanging\"]',2,NULL,'e로 시작, \'서로 바꾸다\'라는 뜻이야'),(9,'exchanging','\'exchange\'는 서로 주고받는다는 뜻이야. passing, trading, swapping도 비슷한 의미로 쓸 수 있어.','subjective',1,33,'\'쪽지를 주고받다\'\n\nWe started _______ notes back and forth.\n(뜻: 서로 주고받다)',NULL,3,'[\"exchanging\", \"passing\", \"trading\", \"swapping\"]','e_______ (8글자, \'교환하다\'라는 뜻)'),(10,'empty','\'empty\'는 물리적으로 비어있다는 뜻뿐만 아니라 마음이 허무하고 공허할 때도 쓸 수 있어.','multiple_choice',1,44,'다음 중 \'허무한, 공허한\'을 뜻하는 영어 단어는?','[\"early\", \"eager\", \"empty\", \"exact\"]',1,NULL,'\'빈\'이라는 뜻도 있어!'),(11,'empty','\'feel empty\'는 허무하고 공허한 감정을 표현할 때 쓰는 자연스러운 영어 표현이야.','subjective',1,44,'\'허무한, 공허한\' = e______\n\nI felt ______ after waiting for nothing.\n(뜻: 아무것도 얻지 못하고 기다린 후 허무했다)',NULL,2,'[\"empty\", \"Empty\"]','e로 시작, \'비다\'라는 뜻의 형용사에서 나온 감정 표현'),(12,'hollow','\'empty\'는 허무하고 공허한 감정을 표현할 때 자주 써. \'hollow\'나 \'deflated\'도 비슷한 의미로 쓸 수 있어.','subjective',1,44,'\'허무했다\'는 감정을 표현할 때\n\nI felt so ______.\n(뜻: 텅 빈 듯한, 공허한 느낌)',NULL,3,'[\"empty\", \"hollow\", \"deflated\", \"let down\"]','h____ (6글자, \'쑥 들어간\' 뜻의 형용사)'),(13,'occasionally','\'occasionally\'는 가끔씩, 때때로라는 뜻이야. 아직도 가끔 쪽지로 대화한다는 상황을 표현할 때 써.','multiple_choice',1,57,'다음 중 \'가끔, 때때로\'를 뜻하는 영어 표현은?','[\"occasionally\", \"officially\", \"ordinarily\", \"obviously\"]',1,NULL,'occasion(특별한 날)의 부사형 — 항상은 아니고 가끔만 일어난다는 뉘앙스'),(14,'occasionally','\'occasionally\'는 \'가끔, 때때로\'라는 뜻의 부사로 일상 대화에서 자주 쓰이는 표현이야.','subjective',1,57,'\'가끔\' = o_________\n\nWe __________ still communicate through notes.\n(뜻: 우리는 가끔 여전히 쪽지로 소통해)',NULL,2,'[\"occasionally\", \"Occasionally\"]','o로 시작하는 부사, \'특별한 occasion\'할 때 그 단어의 부사 버전 🎉'),(15,'now and then','\'now and then\'은 \'가끔씩, 때때로\'라는 뜻이야. from time to time, once in a while도 같은 의미로 쓸 수 있어.','subjective',1,57,'\'가끔씩, 때때로\'\n\nWe still communicate through notes ______ and ______.\n(뜻: 가끔씩)',NULL,3,'[\"now and then\", \"from time to time\", \"every now and then\", \"once in a while\", \"occasionally\"]','n__ a__ t___ (이제와 그때, 시간 표현)'),(16,'occasionally','\'occasionally\'는 가끔씩, 때때로라는 뜻이야. 학생이 가끔 머리를 쥐어뜯는 상황을 표현할 때 쓸 수 있어.','multiple_choice',2,69,'다음 중 \'가끔, 때때로\'를 뜻하는 영어 표현은?','[\"officially\", \"occasionally\", \"obviously\", \"originally\"]',1,NULL,'once in a while과 같은 뜻, \'occasion(기회)\'에서 나온 단어'),(17,'scribbling','\'scribble\'은 급하게 쓰거나 끄적거린다는 뜻이야. 학생이 숙제하면서 연필로 끄적이는 모습을 표현할 때 딱이지.','subjective',2,69,'\'낙서하다/끄적거리다\' = s______\n\nHe was ______ notes in the margin.\n(뜻: 그는 여백에 끄적거리고 있었다)',NULL,2,'[\"scribbling\", \"Scribbling\", \"scribble\"]','s로 시작하는 동사, 긁는다는 뜻도 있어 ✏️'),(18,'tug at','\'tug at his hair\'는 좌절이나 스트레스로 머리카락을 잡아당기는 걸 표현해. pull at도 같은 의미로 쓸 수 있어.','subjective',2,69,'수학 문제가 어려워서 \'머리를 쥐어뜯었다\'\n\nHe would sometimes ______ his hair in frustration.\n(뜻: 머리카락을 잡아당기다, 좌절감에 빠지다)',NULL,3,'[\"tug at\", \"pull at\", \"grab at\", \"clutch at\"]','줄다리기 = tug of war, \'잡아당기다\' + at 🪢'),(19,'empty','\'empty\'는 비어있다는 뜻이야. 학생이 없어서 자리가 텅 빈 상황을 표현할 때 써.','multiple_choice',2,81,'다음 중 \'비어있는, 텅 빈\'을 뜻하는 영어 단어는?','[\"early\", \"equal\", \"enjoy\", \"empty\"]',1,NULL,'e로 시작하는 5글자, 반대말은 full'),(20,'sniffling','\'sniffling\'은 코를 훌쩍거리는 소리를 나타내는 표현이야. 울고 난 후나 감정이 북받칠 때 자주 쓰지.','subjective',2,81,'\'훌쩍거리다\' = s______\n\nShe was quietly ______ while reading her workbook.\n(뜻: 그녀는 문제집을 보며 조용히 훌쩍거리고 있었다)',NULL,2,'[\"sniffling\", \"Sniffling\", \"sniffing\"]','코감기 걸렸을 때 내는 소리 💧'),(21,'glancing over','\'glance over\'는 자꾸 힐끗힐끗 쳐다보는 걸 표현해. keep -ing와 함께 쓰면 반복적인 행동을 나타내지.','subjective',2,81,'\'자꾸 창가를 쳐다봤다\'\n\nYeon-woo kept ______ ______ at the window.\n(뜻: 자꾸 힐끗힐끗 보다)',NULL,3,'[\"glancing over\", \"looking over\", \"peeking over\", \"gazing over\", \"staring over\"]','\'at a glance(한눈에)\' 할 때 그 단어 + over 👀'),(22,'swollen','\'swollen\'은 울거나 다쳐서 부어있는 상태를 표현할 때 써. 눈이 빨갛게 부은 모습이지.','multiple_choice',2,93,'다음 중 \'부어있는, 팽창한\'을 뜻하는 영어 표현은?','[\"swollen\", \"serious\", \"smooth\", \"sleepy\"]',1,NULL,'풍선처럼 부풀어 오른 상태, swell'),(23,'swollen','\'swollen\'은 부어오른 상태를 나타내는 형용사야. 울고 난 후 눈이 부은 모습을 표현할 때 자주 쓰여.','fill_in_blank',2,93,'His eyes were red and ______.','[\"watery\", \"swollen\", \"tired\", \"burning\"]',2,NULL,'s로 시작하는 7글자 단어, 벌에 쏘이면?'),(24,'collapsed down','\'collapsed\'나 \'passed out\'은 갑자기 의식을 잃고 쓰러지는 걸 표현해. \'fainted\'도 같은 의미로 쓸 수 있어.','subjective',2,93,'\'갑자기 쓰러지다\'\n\nHe suddenly ______ ______.\n(뜻: 의식을 잃고 넘어지다)',NULL,3,'[\"collapsed down\", \"passed out\", \"fell down\", \"collapsed\", \"fainted\"]','c_______ d___ (의식을 잃다 + 아래로)'),(25,'give up','\'give up\'은 포기하다라는 뜻이야. 아버지가 딸에게 어려워도 포기하지 말라고 격려하는 상황이지.','multiple_choice',2,105,'다음 중 \'포기하다\'의 영어 표현으로 알맞은 것은?','[\"give off\", \"give out\", \"give away\", \"give up\"]',1,NULL,'항복할 때 손을 위로 올리는 모습을 떠올려봐'),(26,'tucked','\'tuck\'은 무언가를 사이나 틈에 끼워넣는다는 뜻이야. \'tucked between\'은 ~사이에 끼워져 있다는 표현이지.','subjective',2,105,'\'끼우다/사이에 넣다\' = t______\n\nA letter was ______ between the pages.\n(뜻: 편지가 페이지 사이에 끼워져 있었다)',NULL,2,'[\"tucked\", \"Tucked\"]','t로 시작, \'집어넣다\'라는 뜻. 셔츠를 바지에 넣을 때도 써'),(27,'tucked','\'tucked\'는 무언가가 사이에 끼워져 있거나 숨겨져 있는 상태를 표현해. inserted나 placed도 같은 의미로 쓸 수 있어.','subjective',2,105,'\'편지가 끼워져 있었다\'\n\nA letter was ______ between the pages.\n(뜻: ~이 사이에 끼워지다)',NULL,3,'[\"tucked\", \"inserted\", \"placed\", \"stuck\", \"wedged\"]','t_____ (6글자, 갇혀있다는 뜻)'),(28,'closely','\'closely\'는 유심히, 주의 깊게 관찰하거나 보는 것을 의미해. 아버지가 딸의 자리를 자세히 살펴보는 상황이야.','multiple_choice',2,118,'다음 중 \'유심히, 주의 깊게\'를 뜻하는 영어 부사는?','[\"calmly\", \"closely\", \"cleverly\", \"clearly\"]',1,NULL,'\'닫다\'라는 단어와 연관된 부사'),(29,'carefully','\'carefully watched\'나 \'closely watched\'는 유심히, 주의깊게 관찰한다는 뜻이야.','subjective',2,118,'\'유심히 보다\' = c_______ watched\n\nHe ______ watched seat number 3 before leaving without a word.\n(뜻: 그는 3번 자리를 유심히 보더니 아무 말 없이 나갔다)',NULL,2,'[\"carefully\", \"closely\", \"Carefully\", \"Closely\"]','c로 시작하는 \'주의깊게\'라는 부사'),(30,'intently','\'intently\'는 집중해서 유심히 보는 걸 표현해. closely, carefully, attentively도 비슷한 의미로 쓸 수 있어.','subjective',2,118,'\'유심히 보다\'\n\nHe looked at seat #3 ______ and left without saying a word.\n(뜻: 세심하게, 주의 깊게)',NULL,3,'[\"intently\", \"closely\", \"carefully\", \"attentively\"]','i___ly (부사, intense의 부사형)'),(31,'nervous','\'nervous\'는 긴장되고 불안할 때 쓰는 표현이야. 이상한 전화 때문에 민혜가 느끼는 감정이지.','multiple_choice',3,135,'다음 중 \'긴장한, 불안한\'의 영어 표현으로 알맞은 것은?','[\"nervous\", \"negative\", \"numerous\", \"natural\"]',1,NULL,'nerve(신경) + ous, 신경이 곤두설 때의 느낌'),(32,'prank','\'prank call\'은 장난 전화라는 뜻으로, 콜센터에서 자주 받게 되는 성가신 전화를 말해.','subjective',3,135,'\'장난 전화\' = p____ call\n\nMost calls were just ______ calls from drunk people.\n(뜻: 대부분의 전화는 술 취한 사람들의 장난 전화였다)',NULL,2,'[\"prank\", \"Prank\"]','p로 시작, \'허위의/가짜의\'라는 뜻도 있어 📞'),(33,'palms','\'palms\'는 손바닥을 뜻해. 긴장하면 손바닥에 땀이 나는 걸 \'sweaty palms\'라고 표현해.','subjective',3,135,'\'손에 땀이 났다\'\n\nHer ______ were sweaty.\n(뜻: 손바닥이 땀으로 젖었다)',NULL,3,'[\"palms\", \"hands\"]','p___ (5글자, 손바닥을 뜻하는 단어)'),(34,'shake','\'shake\'는 손이 떨리거나 진동하는 것을 표현할 때 쓰는 단어야. 무서워서 손이 떨리는 상황이지.','multiple_choice',3,152,'다음 중 \'떨리다, 진동하다\'의 영어 표현으로 알맞은 것은?','[\"shine\", \"share\", \"shake\", \"smile\"]',1,NULL,'지진이 일어날 때도 사용해'),(35,'goosebumps','\'get goosebumps\'는 무서울 때나 감동받을 때 피부에 소름이 돋는 걸 표현하는 영어 관용구야.','subjective',3,152,'\'소름이 돋다\' = get g______\n\nWhen I heard the strange voice, I got ______ all over my back.\n(뜻: 이상한 목소리를 들었을 때 등에 소름이 돋았다)',NULL,2,'[\"goosebumps\", \"goose bumps\"]','거위의 피부처럼 오돌토돌해진다고 해서 붙은 이름 🪿'),(36,'ran up','\'chills ran up/down one\'s spine\'은 무서워서 등골이 오싹한 느낌을 표현할 때 쓰는 표현이야.','subjective',3,152,'\'등에 소름이 돋았다\'\n\nChills ______ ______ her spine.\n(뜻: 등골이 오싹하다)',NULL,3,'[\"ran up\", \"went up\", \"ran down\", \"went down\", \"shot up\"]','r___ u_ (2단어, 달리다 + 위)'),(37,'shocked','\'shocked\'는 갑작스런 상황에 놀라고 당황한 상태를 말해. 예상치 못한 전화에 민혜가 당황한 거지.','multiple_choice',3,169,'다음 중 \'당황한, 놀란\'의 영어 표현으로 알맞은 것은?','[\"serious\", \"silly\", \"sleepy\", \"shocked\"]',1,NULL,'\'쇼크 받았다\'할 때 그 shock의 과거분사 ⚡'),(38,'panicked','\'panic\'은 당황하다, 공포에 빠지다라는 뜻이야. 과거형은 \'panicked\'로 써.','subjective',3,169,'\'당황하다\' = p______\n\nShe was ______ by the unexpected call.\n(뜻: 그녀는 예상치 못한 전화에 당황했다)',NULL,2,'[\"panicked\", \"Panicked\"]','p로 시작, \'공황\'이라는 단어와 친척 관계 😰'),(39,'flustered','\'flustered\'는 갑작스러운 상황에 당황하고 혼란스러워하는 감정을 표현해. bewildered, confused도 비슷한 의미야.','subjective',3,169,'\'당황했다\'\n\nMinhye was ______.\n(뜻: 혼란스럽고 어찌할 바를 모르는 상태)',NULL,3,'[\"flustered\", \"bewildered\", \"confused\", \"perplexed\", \"rattled\"]','f로 시작하는 7글자, flutter(파닥파닥)처럼 마음이 흔들려서 어쩔 줄 모르는 상태 😵'),(40,'empty','\'empty\'는 아무것도 없이 텅 비어있다는 뜻이야. 사무실에 아무도 없는 상황을 표현할 때 쓰지.','multiple_choice',3,186,'다음 중 \'텅 빈, 비어있는\'을 뜻하는 영어 표현은?','[\"eager\", \"exact\", \"empty\", \"early\"]',1,NULL,'\'MT\'랑 발음이 같아'),(41,'froze','\'freeze\'는 얼다는 뜻뿐만 아니라 공포나 충격으로 몸이 굳어질 때도 써. \'froze\'는 과거형이야.','subjective',3,186,'\'얼어붙다, 마비되다\' = f______\n\nHer hands ______ and wouldn\'t move.\n(뜻: 그녀의 손이 얼어붙어서 움직이지 않았다)',NULL,2,'[\"froze\", \"Froze\"]','겨울왕국 영어 제목의 과거형! 🧊'),(42,'wouldn\'t obey','\'wouldn\'t obey\'는 몸이 내 의지대로 움직이지 않을 때 쓰는 표현이야. wouldn\'t listen이나 wouldn\'t respond도 같은 의미로 쓸 수 있어.','subjective',3,186,'\'손이 말을 듣지 않는다\'\n\nMy hands ______ ______ to me.\n(뜻: 내 의지대로 움직이지 않다)',NULL,3,'[\"wouldn\'t obey\", \"won\'t obey\", \"wouldn\'t listen\", \"won\'t listen\", \"wouldn\'t respond\", \"won\'t respond\"]','w___\'t o___ (거부하다, 복종하지 않다)'),(43,'ring','\'ring\'은 전화벨이나 종이 울리는 소리를 표현할 때 쓰는 동사야. 새로운 야간 근무자에게도 같은 일이 반복되는 무서운 결말이지.','multiple_choice',3,207,'다음 중 \'(전화벨이) 울리다\'를 뜻하는 영어 표현은?','[\"ring\", \"rock\", \"roll\", \"rise\"]',1,NULL,'\'반지, 고리\'랑 같은 단어야'),(44,'ringing','전화벨이 울린다는 \'the phone is ringing\'으로 표현해. 이야기의 무한 반복을 암시하는 마지막 장면이야.','fill_in_blank',3,207,'At 2:47 AM, the phone started ______ again.','[\"sounding\", \"ringing\", \"buzzing\", \"calling\"]',2,NULL,'r으로 시작, 전화가 울릴 때 쓰는 동사'),(45,'is ringing','\'ring\'은 전화벨이나 종이 울릴 때 쓰는 동사야. 현재 진행형으로 \'is ringing\'이 가장 자연스러워.','subjective',3,207,'\'전화벨이 울린다\'\n\nThe phone ______ ______.\n(현재 진행되는 상황을 표현)',NULL,3,'[\"is ringing\", \"rings\", \"starts ringing\", \"begins ringing\"]','r___ (4글자) - 종이나 벨이 소리낼 때 쓰는 동사'),(46,'look around','\'look around\'는 주변을 둘러보거나 살펴본다는 뜻이야. 미란이 이상한 상황에서 주변을 확인하는 거지.','multiple_choice',4,218,'다음 중 \'주변을 살펴보다, 둘러보다\'의 영어 표현으로 알맞은 것은?','[\"look into\", \"look around\", \"look forward\", \"look after\"]',1,'[\"look around\"]','l로 시작하는 동사, \'보다\'의 의미'),(47,'dawn','\'dawn\'은 새벽, 여명을 뜻하는 단어로 해가 떠오르기 시작하는 이른 아침 시간을 나타내.','subjective',4,218,'\'새벽\' = d____\n\nShe arrived at ______ to avoid the crowd.\n(뜻: 그녀는 사람들을 피하려고 새벽에 도착했다)',NULL,2,'[\"dawn\", \"Dawn\"]','d로 시작, 해가 뜨기 직전의 그 시간대 🌅'),(48,'looked around','\'look around\'는 주변을 둘러보며 살펴본다는 뜻이야. glance around, peer around도 비슷한 의미로 쓸 수 있어.','subjective',4,218,'\'주변을 살펴보다\'\n\nMiran ______ ______ to see if anyone was around.\n(뜻: 주위를 둘러보다, 살피다)',NULL,3,'[\"looked around\", \"glanced around\", \"peered around\", \"gazed around\"]','l___ a_____ (2단어, 돌아보면서 확인한다는 뜻)'),(49,'empty','\'empty\'는 자리나 공간이 비어있다는 뜻이야. 7번 자리에 학생이 없어서 빈 상태였다는 거지.','multiple_choice',4,229,'다음 중 \'비어 있는, 빈\'을 뜻하는 영어 단어는?','[\"equal\", \"empty\", \"exact\", \"early\"]',1,'[\"empty\"]','e로 시작하는 5글자 단어'),(50,'record','\'record\'는 기록이라는 뜻으로, 보안 시스템의 출입 기록을 말할 때 자주 쓰이는 단어야.','subjective',4,229,'\'기록, 녹화, 녹음하다\' = r______\n\nThere was no ______ of anyone leaving.\n(뜻: 누군가 나간 기록이 없었다)',NULL,2,'[\"record\", \"Record\"]','r로 시작하는 6글자, 음악 앨범을 만들 때도 쓰는 단어 🎵'),(51,'meant','\'This meant that~\'은 \'이것은 ~을 의미했다\'라는 뜻으로, 상황을 분석하고 결론을 내릴 때 자주 쓰는 표현이야.','subjective',4,229,'\'~라는 뜻이다\'를 영어로 표현하면?\n\nThis ______ that someone was still inside the school.\n(뜻: ~를 의미하다)',NULL,3,'[\"meant\", \"means\", \"indicated\", \"suggested\", \"implied\"]','m_____ (6글자, 평균을 나타내는 수학 용어와 같은 단어)'),(52,'quiet','\'quiet\'는 조용하고 고요한 상태를 나타내는 단어야. 체육관 안이 아무 소리 없이 고요했다는 뜻이지.','multiple_choice',4,240,'다음 중 \'고요한, 조용한\'을 뜻하는 영어 단어는?','[\"quite\", \"quick\", \"quiet\", \"quest\"]',1,'[\"quiet\"]','q로 시작하는 5글자 단어'),(53,'entrance','\'entrance\'는 입구라는 뜻으로, 건물이나 장소로 들어가는 문이나 통로를 말해.','subjective',4,240,'\'입구\' = e_______\n\nThere was a security guard at the ______ of the building.\n(뜻: 건물 입구에 경비원이 있었다)',NULL,2,'[\"entrance\", \"Entrance\"]','e로 시작, \'exit\'의 반대말! 들어가는 곳 🚪'),(54,'lying','\'lying\'은 물건이 바닥에 떨어져 있는 상태를 표현해. dropped, left, sitting도 비슷한 의미로 쓸 수 있어.','subjective',4,240,'\'떨어져 있다\' (물건이 바닥에)\n\nThe shoe was ______ on the ground in front of the gym entrance.\n(뜻: 바닥에 떨어져 있다)',NULL,3,'[\"lying\", \"dropped\", \"left\", \"sitting\"]','l_____ (과거분사, 누워있다는 뜻도 있음)'),(55,'realize','\'realize\'는 갑자기 뭔가를 깨달아서 이해하게 되는 걸 말해. 미란이가 여자의 행동 이유를 깨달은 상황이지.','multiple_choice',4,251,'다음 중 \'깨닫다, 알아차리다\'의 영어 표현으로 알맞은 것은?','[\"reflect\", \"realize\", \"receive\", \"require\"]',1,'[\"realize\"]','r로 시작하는 7글자 단어'),(56,'obituary','\'obituary\'는 신문의 부고란이나 부고 기사를 뜻하는 단어야.','subjective',4,251,'\'부고\' = o______\n\nShe opened the newspaper to the ______ section.\n(뜻: 그녀는 신문의 부고란을 펼쳤다)',NULL,2,'[\"obituary\", \"obituaries\", \"Obituary\"]','죽음을 알리는 신문 섹션, \'ob\'로 시작해'),(57,'obituary','\'obituary section\'은 신문의 부고란을 뜻해. obituary는 사망자의 생애를 기리는 기사를 말하지.','subjective',4,251,'\'부고란\'을 영어로?\n\n______ section\n(뜻: 신문에서 사망 소식을 알리는 란)',NULL,3,'[\"obituary\", \"obituaries\", \"death notice\", \"death notices\"]','o_______ (사망자를 기리는 글, 기억하다는 뜻의 동사와 관련)'),(58,'erase','\'erase\'는 칠판의 글씨나 자국을 지우다는 뜻이야. 이야기의 마지막에 미란이 칠판을 지우며 하루를 마무리하는 장면이지.','multiple_choice',4,263,'다음 중 \'지우다, 없애다\'의 영어 표현으로 알맞은 것은?','[\"escape\", \"erase\", \"expect\", \"excuse\"]',1,'[\"erase\"]','e로 시작하는 5글자 단어'),(59,'erased','\'erase the board\'는 칠판을 지우다라는 뜻으로, 학교에서 자주 쓰이는 표현이야.','subjective',4,263,'\'칠판을 지우다\' = e______ the board\n\nAfter class, the teacher ______ the board.\n(뜻: 수업 후에 선생님이 칠판을 지웠다)',NULL,2,'[\"erased\", \"Erased\"]','e로 시작, \'삭제하다/완전히 없애다\'라는 뜻 📝'),(60,'wiped off','\'wipe off\'는 칠판이나 표면의 글씨를 닦아서 지운다는 뜻이야. erase나 clean off도 같은 의미로 쓸 수 있어.','subjective',4,263,'이야기 마지막 장면에서 \'칠판을 지웠다\'\n\nMiran went back to classroom 3-4 and ______ ______ the blackboard.\n(뜻: 칠판의 글씨를 지우다)',NULL,3,'[\"wiped off\", \"erased\", \"cleaned off\", \"wiped clean\", \"cleared\"]','w___ o__ (2단어, 운동이나 연습할 때도 쓰는 표현)'),(61,'mysterious','\'mysterious\'는 신비롭고 수수께끼 같은 느낌을 주는 성격을 표현할 때 쓰는 단어야.','multiple_choice',5,272,'다음 중 \'신비로운, 불가사의한\'의 영어 표현으로 알맞은 것은?','[\"mechanical\", \"miraculous\", \"magnificent\", \"mysterious\"]',1,NULL,'mystery(미스터리)의 형용사형'),(62,'coincidence','\'coincidence\'는 우연의 일치라는 뜻으로, 예상치 못한 일이 동시에 일어날 때 쓰는 단어야.','subjective',5,272,'\'우연\' = c______\n\nIt was probably just a ______.\n(뜻: 아마 그냥 우연이었을 거야)',NULL,2,'[\"coincidence\", \"Coincidence\"]','동전을 던질 때 쓰는 그 단어와 같아 🪙'),(63,'double majoring','\'double majoring\'은 두 개의 전공을 동시에 하는 것을 뜻해. dual majoring도 같은 의미로 쓸 수 있어.','subjective',5,272,'\'복수전공하다\'\n\nHe is ______ ______ in psychology at art university.\n(뜻: 두 개의 전공을 동시에 하다)',NULL,3,'[\"double majoring\", \"dual majoring\", \"pursuing double major\", \"doing double major\"]','d_____ m_____ (첫 번째 단어는 \'두 배\'라는 뜻)'),(64,'coincidence','\'coincidence\'는 우연히 동시에 일어나는 일을 말해. 꿈 예측이 계속 맞는 상황이 우연인지 궁금해하는 거지.','multiple_choice',5,281,'다음 중 \'우연, 우연의 일치\'를 뜻하는 영어 단어는?','[\"consequence\", \"convenience\", \"coincidence\", \"conference\"]',1,NULL,'coin(동전)이 들어있는 단어 — 동전 던지기처럼 우연히 겹치는 일 🪙'),(65,'tumbled','\'tumble down\'은 굴러떨어지다라는 뜻으로, 계단에서 굴러떨어지는 상황을 표현할 때 자주 쓰는 표현이야.','fill_in_blank',5,281,'I ______ down the stairs in my dream that night.','[\"walked\", \"jumped\", \"tumbled\", \"slipped\"]',2,NULL,'t으로 시작, 종양도 이 단어를 써서 표현해'),(66,'tumbled down','\'tumble down the stairs\'는 계단에서 굴러떨어지다라는 뜻이야. roll down이나 fall down도 비슷한 의미로 쓸 수 있어.','subjective',5,281,'\'계단을 굴러떨어지다\'\n\nI ______ ______ the stairs in my dream.\n(뜻: 계단에서 굴러서 떨어지다)',NULL,3,'[\"tumbled down\", \"rolled down\", \"fell down\", \"tumbled off\", \"rolled off\"]','t_____ d___ (넘어져서 아래로 굴러간다는 뜻)'),(67,'gradually','\'gradually\'는 점점, 서서히 변화하는 걸 표현할 때 쓰는 부사야.','multiple_choice',5,290,'다음 중 \'점점, 서서히\'를 뜻하는 영어 표현은?','[\"gracefully\", \"generally\", \"greatly\", \"gradually\"]',1,NULL,'grade(등급)와 같은 어원 — 한 단계씩 천천히 📶'),(68,'unconscious','\'unconscious\'는 무의식을 뜻하는 심리학 용어야. 꿈과 깊은 연관이 있다고 알려져 있어.','subjective',5,290,'\'무의식\' = u_________\n\nThe _________ mind affects our dreams.\n(뜻: 무의식이 우리 꿈에 영향을 준다)',NULL,2,'[\"unconscious\", \"Unconscious\"]','conscious의 반대, 프로이드가 연구한 의식 아래 숨겨진 마음의 영역 🧠'),(69,'connection','\'connection\'은 두 가지 사이의 연관성이나 연결을 나타내는 단어야. correlation, relationship도 같은 맥락에서 쓸 수 있어.','subjective',5,290,'\'무의식이랑 꿈의 연관성\'\n\nThe ______ between the unconscious mind and dreams\n(뜻: 두 가지 사이의 관련성, 연결고리)',NULL,3,'[\"connection\", \"correlation\", \"relationship\", \"link\", \"association\"]','c_______ (9글자, connect의 명사형)'),(70,'shocking','\'shocking\'은 충격적이고 놀라운 일을 발견했을 때 쓰는 표현이야.','multiple_choice',5,299,'다음 중 \'충격적인, 놀라운\'을 뜻하는 영어 단어는?','[\"shocking\", \"sparkling\", \"sleeping\", \"speaking\"]',1,NULL,'우리나라에서도 놀랄 때 흔히 쓰이는 단어야'),(71,'manipulate','\'manipulate\'는 조작하다, 다루다라는 뜻으로 꿈을 인위적으로 조작한다는 맥락에 딱 맞는 표현이야.','subjective',5,299,'\'조작하다/다루다\' = m______\n\nHe tried to ______ the situation to his advantage.\n(뜻: 그는 자신에게 유리하도록 상황을 조작하려고 했다)',NULL,2,'[\"manipulate\", \"Manipulate\"]','manual(수동의)과 같은 어원 mani(손) — 손으로 교묘히 다루다 ✋'),(72,'according to','\'according to\'는 \'~에 따라, ~별로\'라는 뜻으로 정보를 분류할 때 자주 써. \'by\'도 같은 의미로 쓸 수 있어.','subjective',5,299,'\'날짜별로 정리되어 있다\'\n\nThe dreams were organized ______ ______ date.\n(뜻: ~에 따라, ~별로)',NULL,3,'[\"according to\", \"by\", \"arranged by\", \"organized by\"]','a________ t_ (전치사구, \'~에 따르면\'이란 뜻도 있어)'),(73,'wonder','\'wonder\'는 뭔가에 대해 고민하거나 궁금해할 때 쓰는 표현이야. 어떻게 해야 할지 고민하는 상황이지.','multiple_choice',5,310,'다음 중 \'고민하다, 궁금해하다\'를 뜻하는 영어 표현은?','[\"welcome\", \"wander\", \"whisper\", \"wonder\"]',1,NULL,'\'I wonder why~(왜일까~)\' 할 때 쓰는 동사, wonderful의 원형 ✨'),(74,'should','\'What should I do?\'는 어떻게 해야 할지 모를 때 조언을 구하는 표현이야. \'Should I~?\'로 선택에 대한 고민을 나타낼 수 있어.','subjective',5,310,'\'어떻게 해야 할까요?\' = What s______ I do?\n\n______ I break up with him or give him another chance?\n(뜻: 그와 헤어져야 할까요, 아니면 한 번 더 기회를 줘야 할까요?)',NULL,2,'[\"should\", \"Should\"]','선택의 순간에 쓰는 단어, \'shall\'과 비슷해'),(75,'should do','\'What should I do?\'는 어떻게 해야 할지 모르는 상황에서 조언을 구할 때 쓰는 가장 일반적인 표현이야.','subjective',5,310,'이야기 마지막에서 화자가 고민하며 조언을 구하는 상황\n\nWhat ______ I ______ ?\n(뜻: 어떻게 해야 할까요?)',NULL,3,'[\"should do\", \"shall do\", \"ought to do\"]','s_____ d_ (조언을 구할 때 가장 기본적인 표현)'),(76,'brighten','\'brighten\'은 밝아지거나 활기차게 되는 걸 의미해. 여자친구가 과외 후 표정이 밝아진 상황이지.','multiple_choice',6,320,'다음 중 \'밝아지다, 활기차게 되다\'를 뜻하는 영어 표현은?','[\"bother\", \"broaden\", \"brighten\", \"burden\"]',1,NULL,'전구가 \'밝아질\' 때도 써요'),(77,'tutor','\'tutor\'는 과외선생님이나 개인교습을 해주는 사람을 뜻해.','subjective',6,320,'\'과외\' = t______\n\nHer parents hired a ______ to help with her math.\n(뜻: 부모님이 수학을 도와줄 과외선생님을 고용했다)',NULL,2,'[\"tutor\", \"Tutor\"]','t로 시작, \'teach\'와 같은 어근을 가진 단어'),(78,'hired','\'hire\'는 돈을 주고 사람을 고용한다는 뜻이야. got, found, arranged도 이 맥락에서 쓸 수 있어.','subjective',6,320,'\'과외 선생님을 붙여주다\'\n\nHer parents ______ a private tutor for her.\n(뜻: ~을 고용하다, 채용하다)',NULL,3,'[\"hired\", \"got\", \"found\", \"arranged\", \"provided\"]','h___ (4글자, 고용하다라는 뜻도 있음)'),(79,'awkward','\'awkward\'는 상황이 어색하거나 불편할 때 쓰는 표현이야. 친구가 뭔가 이상하게 웃는 어색한 분위기를 말하는 거지.','multiple_choice',6,330,'다음 중 \'어색한, 불편한\'의 영어 표현으로 알맞은 것은?','[\"awkward\", \"annoying\", \"amazing\", \"anxious\"]',1,NULL,'4개 중 발음이 어려울 거 같은 단어! 😬'),(80,'change the subject','\'change the subject\'는 화제를 바꾸다라는 뜻의 기본 표현이야. 어색한 상황에서 자주 쓰이지.','subjective',6,330,'\'화제를 바꾸다/돌리다\' = c______ the s______\n\nShe tried to ______ the ______ when I asked about her boyfriend.\n(뜻: 남자친구에 대해 물어보자 그녀는 화제를 돌리려 했다)',NULL,2,'[\"change the subject\", \"change subject\", \"Change the subject\", \"Change subject\"]','바꾸다 c + 주제/제목을 뜻하는 s로 시작하는 단어'),(81,'changed','\'change the subject\'는 화제를 돌리다라는 뜻이야. switch나 shift도 같은 의미로 쓸 수 있어.','subjective',6,330,'\'화제를 돌리다\'\n\nShe ______ the subject when I asked about the tutor.\n(뜻: 대화 주제를 바꾸다)',NULL,3,'[\"changed\", \"switched\", \"shifted\", \"diverted\"]','c_____ (전환하다, 바꾸다라는 뜻의 동사)'),(82,'delayed','\'delayed\'는 예정보다 늦어지거나 지연되는 걸 의미해. 과외가 늦어지는 상황에 딱 맞는 표현이야.','multiple_choice',6,340,'다음 중 \'지연되다, 늦어지다\'의 영어 표현으로 알맞은 것은?','[\"decided\", \"delayed\", \"deleted\", \"detailed\"]',1,NULL,'게임에서 \'딜레이 걸렸다\'할 때 그 delay의 과거형 ✈️'),(83,'excuses','\'make excuses\'는 핑계를 대다라는 뜻의 자연스러운 영어 표현이야.','fill_in_blank',6,340,'She always makes ______ when she\'s late.','[\"explanations\", \"excuses\", \"reasons\", \"stories\"]',2,NULL,'\'Excuse me\' 할 때 그 단어의 복수형, make와 짝꿍으로 쓰여'),(84,'making up','\'make up excuses\'는 핑계나 변명을 지어내다라는 뜻이야. come up with도 비슷한 의미로 쓸 수 있어.','subjective',6,340,'\'핑계를 대다\'\n\nShe started ______ ______ excuses like \"I was tired and took a nap.\"\n(뜻: 핑계나 변명을 늘어놓다)',NULL,3,'[\"making up\", \"coming up with\", \"giving\", \"making\"]','m___ u_ (2단어, 만들다ing + 위로)'),(85,'excited','\'excited\'는 기분이 좋아서 들뜨거나 신난 상태를 말해. 목소리에서 설렘이 느껴지는 상황이지.','multiple_choice',6,350,'다음 중 \'들뜬, 신난\'을 뜻하는 영어 표현으로 알맞은 것은?','[\"exhausted\", \"excited\", \"experienced\", \"embarrassed\"]',1,NULL,'e로 시작하는 7글자 단어, \'흥분한\' 느낌'),(86,'well-being','\'ask about someone\'s well-being\'은 안부를 묻다는 뜻의 정중한 표현이야.','subjective',6,350,'\'안부를 묻다\' = ask about someone\'s w______\n\nI called to ask about her ______.\n(뜻: 그녀의 안부를 묻기 위해 전화했다)',NULL,2,'[\"well-being\", \"wellbeing\", \"welfare\"]','건강과 행복을 뜻하는 단어, w로 시작해'),(87,'check up','\'check up on\'은 누군가의 안부나 상황을 확인한다는 뜻이야. check in이나 follow up도 비슷한 의미로 쓸 수 있어.','subjective',6,350,'\'안부 인사차 전화하다\'\n\nI called her mom to ______ ______ on how she was doing.\n(뜻: ~의 안부를 묻다)',NULL,3,'[\"check up\", \"check in\", \"follow up\"]','c___ u_ (2단어, 전화로 상황 확인할 때 쓰는 표현)'),(88,'certain','\'certain\'은 뭔가가 분명하거나 확실할 때 쓰는 표현이야. 의심스러운 상황에서 확신을 나타낼 때 사용해.','multiple_choice',6,360,'다음 중 \'분명한, 확실한\'을 뜻하는 영어 표현으로 알맞은 것은?','[\"central\", \"careful\", \"certain\", \"curious\"]',1,NULL,'cert(증명서) + ain, certificate(자격증)과 같은 어원'),(89,'definitely','\'definitely\'는 \'분명히, 확실히\'라는 뜻으로 확신을 표현할 때 쓰는 부사야.','subjective',6,360,'\'분명한, 확실한\' = d______\n\nSomething is ______ wrong here.\n(뜻: 뭔가 분명히 잘못됐어)',NULL,2,'[\"definitely\", \"Definitely\"]','d로 시작, \'명백히/분명히\'라는 부사로도 쓰여'),(90,'smells fishy','\'Something smells fishy\'는 뭔가 수상하다는 뜻이야. 생선이 상하면 냄새가 나듯이, 상황이 의심스러울 때 쓰는 관용 표현이지.','subjective',6,360,'\'뭔가 수상하다\'\n\nSomething ______ ______.\n(뜻: 뭔가 수상한 냄새가 난다)',NULL,3,'[\"smells fishy\", \"Smells fishy\", \"smells off\", \"seems fishy\"]','s_____ f____ (2단어, 생선 비린내를 떠올려봐 🐟)');
/*!40000 ALTER TABLE `quiz_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quiz_result`
--

DROP TABLE IF EXISTS `quiz_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content_id` bigint NOT NULL,
  `script_id` bigint NOT NULL,
  `difficulty` int NOT NULL,
  `attempt_count` int NOT NULL,
  `hint_used` bit(1) DEFAULT b'0',
  `score` int NOT NULL,
  `lang_code` varchar(5) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_quiz_result_content` (`content_id`),
  KEY `fk_quiz_result_script` (`script_id`),
  KEY `fk_quiz_result_user` (`user_id`),
  CONSTRAINT `fk_quiz_result_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quiz_result_script` FOREIGN KEY (`script_id`) REFERENCES `script` (`script_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quiz_result_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quiz_result`
--

LOCK TABLES `quiz_result` WRITE;
/*!40000 ALTER TABLE `quiz_result` DISABLE KEYS */;
INSERT INTO `quiz_result` VALUES (1,1,1,11,1,1,_binary '\0',20,'2026-05-31 18:03:53'),(2,1,1,22,1,1,_binary '\0',20,'2026-05-31 18:03:53'),(3,1,1,33,1,1,_binary '\0',20,'2026-05-31 18:03:53'),(4,1,1,44,1,1,_binary '\0',20,'2026-05-31 18:03:53'),(5,1,1,57,1,1,_binary '\0',20,'2026-05-31 18:03:53'),(6,1,1,11,1,1,_binary '\0',20,'2026-05-31 18:21:58'),(7,1,1,22,1,1,_binary '\0',20,'2026-05-31 18:21:58'),(8,1,1,33,1,1,_binary '\0',20,'2026-05-31 18:21:58'),(9,1,1,44,1,1,_binary '',16,'2026-05-31 18:21:58'),(10,1,1,57,1,1,_binary '\0',20,'2026-05-31 18:21:58'),(11,1,1,11,1,1,_binary '\0',20,'2026-05-31 18:30:20'),(12,1,1,22,1,1,_binary '\0',20,'2026-05-31 18:30:20'),(13,1,1,33,1,1,_binary '\0',20,'2026-05-31 18:30:20'),(14,1,1,44,1,1,_binary '\0',20,'2026-05-31 18:30:20'),(15,1,1,57,1,3,_binary '\0',8,'2026-05-31 18:30:20'),(16,1,1,11,1,1,_binary '\0',20,'2026-05-31 19:53:55'),(17,1,1,22,1,2,_binary '',10,'2026-05-31 19:53:56'),(18,1,1,33,1,1,_binary '\0',20,'2026-05-31 19:53:56'),(19,1,1,44,1,1,_binary '\0',20,'2026-05-31 19:53:56'),(20,1,1,57,1,2,_binary '\0',14,'2026-05-31 19:53:56'),(21,1,2,69,1,1,_binary '',16,'2026-05-31 20:15:54'),(22,1,2,81,1,1,_binary '\0',20,'2026-05-31 20:15:54'),(23,1,2,93,1,2,_binary '\0',14,'2026-05-31 20:15:54'),(24,1,2,105,1,2,_binary '\0',14,'2026-05-31 20:15:54'),(25,1,2,118,1,3,_binary '\0',8,'2026-05-31 20:15:54'),(26,1,3,135,1,1,_binary '\0',20,'2026-05-31 20:24:24'),(27,1,3,152,1,1,_binary '\0',20,'2026-05-31 20:24:24'),(28,1,3,169,1,2,_binary '\0',14,'2026-05-31 20:24:24'),(29,1,3,186,1,1,_binary '\0',20,'2026-05-31 20:24:24'),(30,1,3,207,1,1,_binary '',16,'2026-05-31 20:24:24'),(31,2,1,11,2,3,_binary '\0',9,'2026-05-31 20:37:01'),(32,2,1,22,2,1,_binary '',17,'2026-05-31 20:37:01'),(33,2,1,33,2,2,_binary '',11,'2026-05-31 20:37:01'),(34,2,1,44,2,3,_binary '\0',9,'2026-05-31 20:37:01'),(35,2,1,57,2,3,_binary '',5,'2026-05-31 20:37:01'),(36,2,2,69,2,3,_binary '\0',0,'2026-05-31 20:44:57'),(37,2,2,81,2,2,_binary '',11,'2026-05-31 20:44:57'),(38,2,2,93,2,3,_binary '\0',9,'2026-05-31 20:44:57'),(39,2,2,105,2,3,_binary '',5,'2026-05-31 20:44:57'),(40,2,2,118,2,1,_binary '\0',20,'2026-05-31 20:44:57'),(41,2,3,135,2,3,_binary '\0',0,'2026-05-31 20:50:48'),(42,2,3,152,2,2,_binary '',11,'2026-05-31 20:50:48'),(43,2,3,169,2,3,_binary '',5,'2026-05-31 20:50:48'),(44,2,3,186,2,1,_binary '',17,'2026-05-31 20:50:48'),(45,2,3,207,2,2,_binary '',11,'2026-05-31 20:50:48'),(46,2,4,218,2,4,_binary '\0',0,'2026-05-31 20:58:53'),(47,2,4,229,2,2,_binary '',11,'2026-05-31 20:58:53'),(48,2,4,240,2,3,_binary '',5,'2026-05-31 20:58:53'),(49,2,4,251,2,1,_binary '',17,'2026-05-31 20:58:53'),(50,2,4,263,2,2,_binary '',11,'2026-05-31 20:58:53'),(51,2,5,272,2,4,_binary '\0',4,'2026-05-31 21:08:27'),(52,2,5,281,2,2,_binary '',11,'2026-05-31 21:08:27'),(53,2,5,290,2,3,_binary '',5,'2026-05-31 21:08:27'),(54,2,5,299,2,1,_binary '',17,'2026-05-31 21:08:27'),(55,2,5,310,2,2,_binary '',11,'2026-05-31 21:08:27'),(56,3,4,218,1,1,_binary '\0',20,'2026-06-01 19:45:28'),(57,3,4,229,1,1,_binary '\0',20,'2026-06-01 19:45:28'),(58,3,4,240,1,1,_binary '\0',20,'2026-06-01 19:45:28'),(59,3,4,251,1,1,_binary '\0',20,'2026-06-01 19:45:28'),(60,3,4,263,1,1,_binary '\0',20,'2026-06-01 19:45:28'),(61,3,3,135,1,1,_binary '\0',20,'2026-06-01 20:08:19'),(62,3,3,152,1,1,_binary '',16,'2026-06-01 20:08:19'),(63,3,3,169,1,1,_binary '\0',20,'2026-06-01 20:08:19'),(64,3,3,186,1,1,_binary '\0',20,'2026-06-01 20:08:19'),(65,3,3,207,1,1,_binary '\0',20,'2026-06-01 20:08:19'),(66,3,1,11,1,1,_binary '\0',20,'2026-06-02 15:13:19'),(67,3,1,22,1,2,_binary '\0',14,'2026-06-02 15:13:19'),(68,3,1,33,1,1,_binary '',16,'2026-06-02 15:13:19'),(69,3,1,44,1,1,_binary '\0',20,'2026-06-02 15:13:19'),(70,3,1,57,1,1,_binary '\0',20,'2026-06-02 15:13:19'),(71,3,4,218,1,1,_binary '\0',20,'2026-06-02 15:25:57'),(72,3,4,229,1,1,_binary '\0',20,'2026-06-02 15:25:57'),(73,3,4,240,1,1,_binary '\0',20,'2026-06-02 15:25:57'),(74,3,4,251,1,1,_binary '\0',20,'2026-06-02 15:25:57'),(75,3,4,263,1,1,_binary '\0',20,'2026-06-02 15:25:57');
/*!40000 ALTER TABLE `quiz_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `score_table`
--

DROP TABLE IF EXISTS `score_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `score_table` (
  `difficulty` int NOT NULL,
  `attempt` int NOT NULL,
  `hint_used` bit(1) NOT NULL,
  `score` int NOT NULL,
  PRIMARY KEY (`difficulty`,`attempt`,`hint_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `score_table`
--

LOCK TABLES `score_table` WRITE;
/*!40000 ALTER TABLE `score_table` DISABLE KEYS */;
INSERT INTO `score_table` VALUES (1,1,_binary '\0',20),(1,1,_binary '',16),(1,2,_binary '\0',14),(1,2,_binary '',10),(1,3,_binary '\0',8),(1,3,_binary '',4),(1,4,_binary '\0',3),(2,1,_binary '\0',20),(2,1,_binary '',17),(2,2,_binary '\0',15),(2,2,_binary '',11),(2,3,_binary '\0',9),(2,3,_binary '',5),(2,4,_binary '\0',4),(3,1,_binary '\0',20),(3,1,_binary '',18),(3,2,_binary '\0',16),(3,2,_binary '',12),(3,3,_binary '\0',10),(3,3,_binary '',6),(3,4,_binary '\0',5);
/*!40000 ALTER TABLE `score_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `script`
--

DROP TABLE IF EXISTS `script`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `script` (
  `script_id` bigint NOT NULL AUTO_INCREMENT,
  `character_name` varchar(50) DEFAULT NULL,
  `has_options` bit(1) DEFAULT NULL,
  `script_content` text,
  `sequence_num` int DEFAULT NULL,
  `content_id` bigint NOT NULL,
  `option_id` bigint DEFAULT NULL,
  `chapter_id` bigint NOT NULL,
  PRIMARY KEY (`script_id`),
  KEY `FK5ddbrpmkfuirg0d8d01yf19eq` (`option_id`),
  KEY `fk_script_content` (`content_id`),
  KEY `FKabfkmacdejspy8h2427gjt7sf` (`chapter_id`),
  CONSTRAINT `FK5ddbrpmkfuirg0d8d01yf19eq` FOREIGN KEY (`option_id`) REFERENCES `script_option` (`option_id`),
  CONSTRAINT `fk_script_content` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKabfkmacdejspy8h2427gjt7sf` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`chapter_id`)
) ENGINE=InnoDB AUTO_INCREMENT=361 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `script`
--

LOCK TABLES `script` WRITE;
/*!40000 ALTER TABLE `script` DISABLE KEYS */;
INSERT INTO `script` VALUES (1,NULL,_binary '\0','나 대학교 다닐 때 시험기간마다 도서관 3층 64번 자리에만 앉았음.',1,1,NULL,1),(2,NULL,_binary '\0','창가 자리라서 햇빛도 잘 들고 에어컨 바람도 안 와서 딱 좋았거든.',2,1,NULL,1),(3,NULL,_binary '\0','근데 어느 날부터 그 자리에 항상 먼저 와있는 애가 있는 거임.',3,1,NULL,1),(4,NULL,_binary '\0','65번 자리에 앉아서 책 읽고 있더라고.',4,1,NULL,1),(5,NULL,_binary '\0','처음엔 짜증났음. 내 자리 옆에 왜 앉냐고.',5,1,NULL,1),(6,NULL,_binary '\0','그런데 그 애가 책상에 A4 용지 하나를 올려두고 가는 거야.',6,1,NULL,1),(7,NULL,_binary '\0','거기에 연필로 \'64번 자리 비워둘게요\' 이렇게 적어둔 거임.',7,1,NULL,1),(8,NULL,_binary '\0','뭔가 이상하다 싶었는데 일단 고맙긴 해서 그냥 앉았음.',8,1,NULL,1),(9,NULL,_binary '\0','다음 날도 똑같더라고. 65번에 그 애 있고, 64번엔 쪽지.',9,1,NULL,1),(10,NULL,_binary '\0','근데 이번엔 \'오늘 비 온대요. 우산 챙기세요\' 이렇게 적혀있음.',10,1,NULL,1),(11,NULL,_binary '\0','이게 뭐지? 싶어서 슬쩍 옆을 봤는데 그 애가 책에 집중하고 있어서 말 걸기 애매했음.',11,1,NULL,1),(12,NULL,_binary '\0','그날 정말 비 왔음. 우산 안 가져갔으면 비 맞을 뻔했어.',12,1,NULL,1),(13,NULL,_binary '\0','그 다음 날부터 호기심이 생기기 시작함.',13,1,NULL,1),(14,NULL,_binary '\0','쪽지 내용이 매일 달라지거든.',14,1,NULL,1),(15,NULL,_binary '\0','\'오늘 학식 돈까스 맛없다고 하네요\', \'3층 화장실 휴지 떨어졌어요\'',15,1,NULL,1),(16,NULL,_binary '\0','이런 식으로 은근 유용한 정보들이었음.',16,1,NULL,1),(17,NULL,_binary '\0','한 달 정도 지났을 때 그 애 얼굴을 제대로 본 적이 없다는 걸 깨달았어.',17,1,NULL,1),(18,NULL,_binary '\0','항상 내가 올 때는 이미 책 보고 있고, 내가 자리 뜰 때는 아직 앉아있고.',18,1,NULL,1),(19,NULL,_binary '\0','궁금해서 일부러 일찍 가봤는데도 이미 앉아있더라.',19,1,NULL,1),(20,NULL,_binary '\0','도대체 몇 시에 오는 거야?',20,1,NULL,1),(21,NULL,_binary '\0','어느 날 쪽지에 \'시험 잘 보세요. 항상 열심히 하시는 모습 멋있어요\' 이렇게 적혀있는 거임.',21,1,NULL,1),(22,NULL,_binary '\0','이때 뭔가 심장이 두근거렸어.',22,1,NULL,1),(23,NULL,_binary '\0','그래서 용기내서 쪽지 밑에 작은 글씨로 \'고마워요\' 이렇게 적어뒀음.',23,1,NULL,1),(24,NULL,_binary '\0','다음 날 가보니까 내 글씨 옆에 이모티콘 하나 그려져 있었어.',24,1,NULL,1),(25,NULL,_binary '\0','웃는 얼굴이었는데 되게 귀엽게 그렸더라고.',25,1,NULL,1),(26,NULL,_binary '\0','그날부터 우리 쪽지 주고받기 시작함.',26,1,NULL,1),(27,NULL,_binary '\0','근데 진짜 대화는 안 해. 계속 쪽지로만.',27,1,NULL,1),(28,NULL,_binary '\0','\'오늘 뭐 공부해요?\' \'전공이 뭐예요?\' 이런 것들.',28,1,NULL,1),(29,NULL,_binary '\0','알고 보니까 그 애는 미술과였음.',29,1,NULL,1),(30,NULL,_binary '\0','그래서 쪽지에 작은 그림도 그려주고 글씨도 예쁘게 썼던 거더라.',30,1,NULL,1),(31,NULL,_binary '\0','나는 경영학과라고 했더니 다음 날 쪽지에 작은 계산기 그림이 그려져 있었어 ㅋㅋㅋ',31,1,NULL,1),(32,NULL,_binary '\0','시험이 끝나고 방학이 되니까 도서관에 갈 일이 없어짐.',32,1,NULL,1),(33,NULL,_binary '\0','근데 그 애가 궁금한 거야.',33,1,NULL,1),(34,NULL,_binary '\0','그래서 개강하고 바로 도서관 갔는데 65번 자리가 비어있더라.',34,1,NULL,1),(35,NULL,_binary '\0','64번에 쪽지도 없고.',35,1,NULL,1),(36,NULL,_binary '\0','혹시 시간표가 바뀐 건가 싶어서 계속 다녀봤는데 한 주 내내 없었어.',36,1,NULL,1),(37,NULL,_binary '\0','그러다가 일주일 뒤에 64번 자리에 쪽지 하나가 놓여있는 거 발견함.',37,1,NULL,1),(38,NULL,_binary '\0','\'미안해요. 교환학생으로 가게 됐어요. 1년 후에 돌아와요\'',38,1,NULL,1),(39,NULL,_binary '\0','그 밑에 작은 비행기 그림하나 그려져 있었음.',39,1,NULL,1),(40,NULL,_binary '\0','진짜 허무했어. 얼굴도 제대로 못 본 채로 헤어진 거잖아.',40,1,NULL,1),(41,NULL,_binary '\0','그 뒤로 도서관 가면 65번 자리만 계속 쳐다봤어.',41,1,NULL,1),(42,NULL,_binary '\0','1년이 지나고 그 애가 돌아왔는지 확인하러 갔는데 또 없더라.',42,1,NULL,1),(43,NULL,_binary '\0','근데 며칠 뒤에 도서관 앞 게시판에 전시회 포스터 하나가 붙어있는 거 봤어.',43,1,NULL,1),(44,NULL,_binary '\0','미술과 졸업전시회였는데 포스터 한쪽 모서리에 작은 글씨로 \'64\'라고 적혀있었음.',44,1,NULL,1),(45,NULL,_binary '\0','그 글씨체가 쪽지랑 똑같았어.',45,1,NULL,1),(46,NULL,_binary '\0','전시회 가봤는데 작품 중에 도서관 그림이 하나 있더라고.',46,1,NULL,1),(47,NULL,_binary '\0','3층 창가 자리를 그린 건데 64번 책상 위에 작은 쪽지들이 쌓여있는 그림이었어.',47,1,NULL,1),(48,NULL,_binary '\0','그림 제목이 \'말 못한 인사\'였음.',48,1,NULL,1),(49,NULL,_binary '\0','그때 옆에서 누군가 말을 거는 거야. \'그 자리 아세요?\'',49,1,NULL,1),(50,NULL,_binary '\0','돌아보니까 익숙한데 낯선 얼굴이었어.',50,1,NULL,1),(51,NULL,_binary '\0','1년 동안 매일 옆에 앉았던 그 애였음.',51,1,NULL,1),(52,NULL,_binary '\0','\'1년 내내 인사하고 싶었는데 용기가 안 났어요\'라고 하더라.',52,1,NULL,1),(53,NULL,_binary '\0','나도 똑같았다고 했어.',53,1,NULL,1),(54,NULL,_binary '\0','그 애가 웃으면서 \'이제 쪽지 말고 진짜 대화할 수 있을까요?\' 이러는데 목소리가 떨리고 있었음.',54,1,NULL,1),(55,NULL,_binary '\0','나도 떨렸어.',55,1,NULL,1),(56,NULL,_binary '\0','지금도 그 애랑 만나고 있음.',56,1,NULL,1),(57,NULL,_binary '\0','아직도 가끔 쪽지로 대화해 ㅋㅋㅋ',57,1,NULL,1),(58,NULL,_binary '\0','연우는 동네 서점에서 일했다.',1,2,NULL,2),(59,NULL,_binary '\0','작은 서점이라 손님이 많지 않았다.',2,2,NULL,2),(60,NULL,_binary '\0','그런데 한 명만 매일 왔다.',3,2,NULL,2),(61,NULL,_binary '\0','학교 끝나고 오는 고등학생.',4,2,NULL,2),(62,NULL,_binary '\0','항상 같은 자리에 앉았다.',5,2,NULL,2),(63,NULL,_binary '\0','창가 쪽 3번 관람석이라고 적힌 의자.',6,2,NULL,2),(64,NULL,_binary '\0','책을 읽는 게 아니라 숙제를 했다.',7,2,NULL,2),(65,NULL,_binary '\0','수학 문제집을 펼쳐놓고 연필로 끄적거렸다.',8,2,NULL,2),(66,NULL,_binary '\0','가끔 머리를 쥐어뜯기도 했다.',9,2,NULL,2),(67,NULL,_binary '\0','연우는 그 학생을 \'3번 학생\'이라고 불렀다.',10,2,NULL,2),(68,NULL,_binary '\0','혼자서.',11,2,NULL,2),(69,NULL,_binary '\0','어느 날 그 학생이 울고 있었다.',12,2,NULL,2),(70,NULL,_binary '\0','조용히 훌쩍거리면서 문제집을 보고 있었다.',13,2,NULL,2),(71,NULL,_binary '\0','연우가 티슈를 갖다 줬다.',14,2,NULL,2),(72,NULL,_binary '\0','학생이 고맙다고 작게 말했다.',15,2,NULL,2),(73,NULL,_binary '\0','그 다음 날부터 인사를 했다.',16,2,NULL,2),(74,NULL,_binary '\0','들어올 때 고개를 살짝 숙이고 나갈 때도.',17,2,NULL,2),(75,NULL,_binary '\0','한 달쯤 지났을까.',18,2,NULL,2),(76,NULL,_binary '\0','그 학생이 안 왔다.',19,2,NULL,2),(77,NULL,_binary '\0','3번 자리가 텅 비어 있었다.',20,2,NULL,2),(78,NULL,_binary '\0','이틀째도 안 왔다.',21,2,NULL,2),(79,NULL,_binary '\0','연우는 자꾸 창가를 쳐다봤다.',22,2,NULL,2),(80,NULL,_binary '\0','일주일이 지나서야 왔다.',23,2,NULL,2),(81,NULL,_binary '\0','그런데 뭔가 달랐다.',24,2,NULL,2),(82,NULL,_binary '\0','머리를 짧게 잘랐고 교복이 아닌 검은 옷을 입고 있었다.',25,2,NULL,2),(83,NULL,_binary '\0','평소처럼 3번 자리에 앉았는데 숙제를 안 꺼냈다.',26,2,NULL,2),(84,NULL,_binary '\0','그냥 멍하니 창밖만 보고 있었다.',27,2,NULL,2),(85,NULL,_binary '\0','연우가 다가가서 물었다.',28,2,NULL,2),(86,NULL,_binary '\0','괜찮냐고.',29,2,NULL,2),(87,NULL,_binary '\0','학생이 고개를 들었다.',30,2,NULL,2),(88,NULL,_binary '\0','눈이 빨갛게 부어 있었다.',31,2,NULL,2),(89,NULL,_binary '\0','아버지가 돌아가셨다고 말했다.',32,2,NULL,2),(90,NULL,_binary '\0','갑자기 쓰러지셨다고.',33,2,NULL,2),(91,NULL,_binary '\0','연우는 뭐라고 말해야 할지 몰랐다.',34,2,NULL,2),(92,NULL,_binary '\0','그냥 옆에 앉았다.',35,2,NULL,2),(93,NULL,_binary '\0','학생이 또 말했다.',36,2,NULL,2),(94,NULL,_binary '\0','여기서 숙제하면서 아버지 생각이 많이 났다고.',37,2,NULL,2),(95,NULL,_binary '\0','아버지도 수학을 좋아하셨다고.',38,2,NULL,2),(96,NULL,_binary '\0','연우가 물었다.',39,2,NULL,2),(97,NULL,_binary '\0','그래서 수학을 하는 거냐고.',40,2,NULL,2),(98,NULL,_binary '\0','학생이 고개를 저었다.',41,2,NULL,2),(99,NULL,_binary '\0','사실 수학을 제일 싫어한다고.',42,2,NULL,2),(100,NULL,_binary '\0','그런데 아버지가 수학 선생님이셨다고.',43,2,NULL,2),(101,NULL,_binary '\0','아버지 책상에서 이 문제집을 찾았다고.',44,2,NULL,2),(102,NULL,_binary '\0','첫 페이지에 편지가 끼워져 있었다고.',45,2,NULL,2),(103,NULL,_binary '\0','학생이 가방에서 편지를 꺼냈다.',46,2,NULL,2),(104,NULL,_binary '\0','연우에게 보여줬다.',47,2,NULL,2),(105,NULL,_binary '\0','\'수학이 어려워도 포기하지 마. 아빠가 항상 응원할게. - 사랑하는 딸 지은이에게\'',48,2,NULL,2),(106,NULL,_binary '\0','편지 아래쪽에 작은 글씨로 또 적혀 있었다.',49,2,NULL,2),(107,NULL,_binary '\0','\'3번 자리에 앉으면 집중이 잘 될 거야.\'',50,2,NULL,2),(108,NULL,_binary '\0','연우가 깨달았다.',51,2,NULL,2),(109,NULL,_binary '\0','아버지가 이 서점을 알고 있었다는 걸.',52,2,NULL,2),(110,NULL,_binary '\0','지은이가 여기서 공부한다는 걸 아셨다는 걸.',53,2,NULL,2),(111,NULL,_binary '\0','지은이도 같은 생각이었나 보다.',54,2,NULL,2),(112,NULL,_binary '\0','눈물을 흘리면서 말했다.',55,2,NULL,2),(113,NULL,_binary '\0','아버지가 여기까지 찾아오셨을지도 모른다고.',56,2,NULL,2),(114,NULL,_binary '\0','딸이 어디서 공부하는지 궁금해서.',57,2,NULL,2),(115,NULL,_binary '\0','연우는 그제서야 기억했다.',58,2,NULL,2),(116,NULL,_binary '\0','한 달 전쯤 한 번 온 중년 남자를.',59,2,NULL,2),(117,NULL,_binary '\0','지은이와 닮은 얼굴이었다.',60,2,NULL,2),(118,NULL,_binary '\0','3번 자리를 유심히 보더니 아무 말 없이 나갔던.',61,2,NULL,2),(119,NULL,_binary '\0','민혜는 콜센터에서 일한다.',1,3,NULL,3),(120,NULL,_binary '\0','야간 근무를 맡은 지 3개월째.',2,3,NULL,3),(121,NULL,_binary '\0','새벽 시간대라 전화가 거의 안 온다.',3,3,NULL,3),(122,NULL,_binary '\0','대부분 술 취한 사람들의 장난 전화나 잘못 건 전화.',4,3,NULL,3),(123,NULL,_binary '\0','그런데 지난주부터 이상한 전화가 오기 시작했다.',5,3,NULL,3),(124,NULL,_binary '\0','새벽 2시 47분. 정확히 같은 시각.',6,3,NULL,3),(125,NULL,_binary '\0','전화벨이 울린다.',7,3,NULL,3),(126,NULL,_binary '\0','받으면 숨소리만 들린다.',8,3,NULL,3),(127,NULL,_binary '\0','\"안녕하세요, 고객센터입니다\" 말해도 대답이 없다.',9,3,NULL,3),(128,NULL,_binary '\0','그냥 후-후- 하는 숨소리.',10,3,NULL,3),(129,NULL,_binary '\0','처음에는 장난 전화려니 했다.',11,3,NULL,3),(130,NULL,_binary '\0','근데 매일 같은 시간이었다.',12,3,NULL,3),(131,NULL,_binary '\0','2시 47분. 정확히.',13,3,NULL,3),(132,NULL,_binary '\0','오늘도 그 시간이 다가온다.',14,3,NULL,3),(133,NULL,_binary '\0','시계를 보니 2시 46분.',15,3,NULL,3),(134,NULL,_binary '\0','민혜는 손에 땀이 났다.',16,3,NULL,3),(135,NULL,_binary '\0','왜 이렇게 긴장하고 있는 걸까?',17,3,NULL,3),(136,NULL,_binary '\0','그냥 끊어버리면 되는데.',18,3,NULL,3),(137,NULL,_binary '\0','2시 47분. 벨이 울린다.',19,3,NULL,3),(138,NULL,_binary '\0','손이 떨렸지만 받았다.',20,3,NULL,3),(139,NULL,_binary '\0','\"안녕하세요...\"',21,3,NULL,3),(140,NULL,_binary '\0','역시 숨소리만 들린다.',22,3,NULL,3),(141,NULL,_binary '\0','그런데 오늘은 뭔가 달랐다.',23,3,NULL,3),(142,NULL,_binary '\0','숨소리 뒤로 다른 소리가 섞여 있었다.',24,3,NULL,3),(143,NULL,_binary '\0','뚝뚝뚝.',25,3,NULL,3),(144,NULL,_binary '\0','물 떨어지는 소리? 아니다.',26,3,NULL,3),(145,NULL,_binary '\0','타자 치는 소리였다.',27,3,NULL,3),(146,NULL,_binary '\0','누군가 키보드를 치고 있었다.',28,3,NULL,3),(147,NULL,_binary '\0','\"거기 누구세요?\" 민혜가 물었다.',29,3,NULL,3),(148,NULL,_binary '\0','타자 소리가 멈췄다.',30,3,NULL,3),(149,NULL,_binary '\0','그리고 목소리가 들렸다.',31,3,NULL,3),(150,NULL,_binary '\0','\"도와줘.\"',32,3,NULL,3),(151,NULL,_binary '\0','여자 목소리였다. 떨리고 있었다.',33,3,NULL,3),(152,NULL,_binary '\0','민혜는 등에 소름이 돋았다.',34,3,NULL,3),(153,NULL,_binary '\0','\"어디에 계세요? 무슨 일이에요?\"',35,3,NULL,3),(154,NULL,_binary '\0','\"찾지 마. 위험해.\"',36,3,NULL,3),(155,NULL,_binary '\0','그리고 전화가 끊어졌다.',37,3,NULL,3),(156,NULL,_binary '\0','발신번호를 확인했다.',38,3,NULL,3),(157,NULL,_binary '\0','표시되지 않음.',39,3,NULL,3),(158,NULL,_binary '\0','민혜는 팀장에게 보고했다.',40,3,NULL,3),(159,NULL,_binary '\0','\"그냥 장난 전화겠지. 신경 쓰지 마.\"',41,3,NULL,3),(160,NULL,_binary '\0','하지만 신경 쓰이지 않을 수가 없었다.',42,3,NULL,3),(161,NULL,_binary '\0','그 여자의 목소리가 계속 맴돌았다.',43,3,NULL,3),(162,NULL,_binary '\0','다음 날 밤.',44,3,NULL,3),(163,NULL,_binary '\0','2시 47분. 또 전화가 왔다.',45,3,NULL,3),(164,NULL,_binary '\0','이번에는 바로 그 목소리가 들렸다.',46,3,NULL,3),(165,NULL,_binary '\0','\"왜 찾으려고 해?\"',47,3,NULL,3),(166,NULL,_binary '\0','민혜는 당황했다.',48,3,NULL,3),(167,NULL,_binary '\0','\"찾는 게 아니라... 도와주려고...\"',49,3,NULL,3),(168,NULL,_binary '\0','\"내 뒤에 있어. 지금.\"',50,3,NULL,3),(169,NULL,_binary '\0','민혜는 뒤를 돌아봤다.',51,3,NULL,3),(170,NULL,_binary '\0','아무도 없었다.',52,3,NULL,3),(171,NULL,_binary '\0','텅 빈 사무실.',53,3,NULL,3),(172,NULL,_binary '\0','\"장난하지 마세요.\"',54,3,NULL,3),(173,NULL,_binary '\0','\"장난이 아니야. 정말로 뒤에 있어.\"',55,3,NULL,3),(174,NULL,_binary '\0','전화기 너머로 또 다른 소리가 들렸다.',56,3,NULL,3),(175,NULL,_binary '\0','의자 바퀴 굴러가는 소리.',57,3,NULL,3),(176,NULL,_binary '\0','민혜는 고개를 들었다.',58,3,NULL,3),(177,NULL,_binary '\0','자신의 의자가 천천히 움직이고 있었다.',59,3,NULL,3),(178,NULL,_binary '\0','아무도 건드리지 않았는데.',60,3,NULL,3),(179,NULL,_binary '\0','전화기에서 웃음소리가 들렸다.',61,3,NULL,3),(180,NULL,_binary '\0','\"이제 알겠지?\"',62,3,NULL,3),(181,NULL,_binary '\0','민혜는 전화기를 내려놓으려 했다.',63,3,NULL,3),(182,NULL,_binary '\0','하지만 손이 말을 듣지 않았다.',64,3,NULL,3),(183,NULL,_binary '\0','전화기가 귀에 붙어 떨어지지 않았다.',65,3,NULL,3),(184,NULL,_binary '\0','목소리가 다시 들렸다.',66,3,NULL,3),(185,NULL,_binary '\0','\"3개월 전에 죽었어. 이 자리에서.\"',67,3,NULL,3),(186,NULL,_binary '\0','민혜는 기억났다.',68,3,NULL,3),(187,NULL,_binary '\0','전임자가 갑자기 그만뒀다고 들었다.',69,3,NULL,3),(188,NULL,_binary '\0','아니다. 그만둔 게 아니었다.',70,3,NULL,3),(189,NULL,_binary '\0','\"야간 근무 중에 심장마비로...\"',71,3,NULL,3),(190,NULL,_binary '\0','팀장의 말이 떠올랐다.',72,3,NULL,3),(191,NULL,_binary '\0','\"넌 내 자리에 앉아 있어.\"',73,3,NULL,3),(192,NULL,_binary '\0','민혜는 의자에서 일어나려 했다.',74,3,NULL,3),(193,NULL,_binary '\0','몸이 움직이지 않았다.',75,3,NULL,3),(194,NULL,_binary '\0','전화기 너머로 속삭임이 들렸다.',76,3,NULL,3),(195,NULL,_binary '\0','\"이제 네 차례야.\"',77,3,NULL,3),(196,NULL,_binary '\0','민혜의 심장이 빠르게 뛰기 시작했다.',78,3,NULL,3),(197,NULL,_binary '\0','숨이 점점 가빠졌다.',79,3,NULL,3),(198,NULL,_binary '\0','가슴이 아팠다.',80,3,NULL,3),(199,NULL,_binary '\0','전화기에서 마지막 목소리가 들렸다.',81,3,NULL,3),(200,NULL,_binary '\0','\"고마워. 이제 나는 자유야.\"',82,3,NULL,3),(201,NULL,_binary '\0','민혜는 의자에 고개를 떨어뜨렸다.',83,3,NULL,3),(202,NULL,_binary '\0','다음 날 아침, 팀장이 발견했다.',84,3,NULL,3),(203,NULL,_binary '\0','심장마비였다.',85,3,NULL,3),(204,NULL,_binary '\0','그리고 한 달 후.',86,3,NULL,3),(205,NULL,_binary '\0','새로운 야간 근무자가 들어왔다.',87,3,NULL,3),(206,NULL,_binary '\0','새벽 2시 47분.',88,3,NULL,3),(207,NULL,_binary '\0','전화벨이 울린다.',89,3,NULL,3),(208,NULL,_binary '\0','학교 청소부 박미란은 새벽 6시에 출근했다.',1,4,NULL,4),(209,NULL,_binary '\0','1층 복도를 걸어가다가 발견했다.',2,4,NULL,4),(210,NULL,_binary '\0','빨간 구두 한 짝.',3,4,NULL,4),(211,NULL,_binary '\0','여학생 실내화가 아니었다. 성인용 하이힐이었다.',4,4,NULL,4),(212,NULL,_binary '\0','미란은 주변을 살펴봤다.',5,4,NULL,4),(213,NULL,_binary '\0','복도는 어젯밤에 깨끗이 청소했던 그대로였다.',6,4,NULL,4),(214,NULL,_binary '\0','아무도 없는 새벽 학교에 누가?',7,4,NULL,4),(215,NULL,_binary '\0','구두는 3학년 4반 교실 문 앞에 놓여 있었다.',8,4,NULL,4),(216,NULL,_binary '\0','한 짝만. 오른쪽.',9,4,NULL,4),(217,NULL,_binary '\0','미란이 교실 문을 열었다.',10,4,NULL,4),(218,NULL,_binary '\0','칠판에 분필로 뭔가가 적혀 있었다.',11,4,NULL,4),(219,NULL,_binary '\0','\"7번 책상을 봐.\"',12,4,NULL,4),(220,NULL,_binary '\0','7번 자리는 비어 있었다. 원래 결석이 많은 학생이었다.',13,4,NULL,4),(221,NULL,_binary '\0','책상 위에 종이 한 장이 접혀 있었다.',14,4,NULL,4),(222,NULL,_binary '\0','\"체육관으로 와. 혼자.\"',15,4,NULL,4),(223,NULL,_binary '\0','미란은 휴대폰으로 경비실에 연락했다.',16,4,NULL,4),(224,NULL,_binary '\0','\"김 아저씨, 어젯밤에 학교에 누가 들어왔나요?\"',17,4,NULL,4),(225,NULL,_binary '\0','\"아뇨. 저 밤새 깨어있었는데 아무도 안 왔어요.\"',18,4,NULL,4),(226,NULL,_binary '\0','그런데 보안 기록을 확인해보니 이상한 점이 있었다.',19,4,NULL,4),(227,NULL,_binary '\0','어젯밤 11시 47분에 정문이 열렸다가 닫혔다.',20,4,NULL,4),(228,NULL,_binary '\0','하지만 다시 나간 기록은 없었다.',21,4,NULL,4),(229,NULL,_binary '\0','누군가 아직 학교 안에 있다는 뜻이었다.',22,4,NULL,4),(230,NULL,_binary '\0','체육관으로 가는 길에 미란은 또 발견했다.',23,4,NULL,4),(231,NULL,_binary '\0','왼쪽 구두.',24,4,NULL,4),(232,NULL,_binary '\0','체육관 입구 앞에 떨어져 있었다.',25,4,NULL,4),(233,NULL,_binary '\0','맨발로 들어간 거였다.',26,4,NULL,4),(234,NULL,_binary '\0','체육관 안은 고요했다.',27,4,NULL,4),(235,NULL,_binary '\0','미란이 전등을 켰다.',28,4,NULL,4),(236,NULL,_binary '\0','농구대 밑에 누군가 앉아 있었다.',29,4,NULL,4),(237,NULL,_binary '\0','20대 여성이었다. 긴 머리, 교복이 아닌 정장 차림.',30,4,NULL,4),(238,NULL,_binary '\0','맨발이었다.',31,4,NULL,4),(239,NULL,_binary '\0','\"누구세요? 여기서 뭐 하는 거예요?\"',32,4,NULL,4),(240,NULL,_binary '\0','여자가 고개를 들었다.',33,4,NULL,4),(241,NULL,_binary '\0','\"전 이 학교 졸업생이에요. 17년 전에.\"',34,4,NULL,4),(242,NULL,_binary '\0','\"밤에 왜 학교에?\"',35,4,NULL,4),(243,NULL,_binary '\0','\"3학년 4반 7번 자리에 앉던 학생이었어요.\"',36,4,NULL,4),(244,NULL,_binary '\0','미란이 깨달았다.',37,4,NULL,4),(245,NULL,_binary '\0','구두를 벗고 들어온 이유.',38,4,NULL,4),(246,NULL,_binary '\0','\"선생님한테 혼나지 않으려고 맨발로 다녔던 거군요.\"',39,4,NULL,4),(247,NULL,_binary '\0','여자가 고개를 끄덕였다.',40,4,NULL,4),(248,NULL,_binary '\0','\"그때처럼 조용히 들어오고 싶었어요.\"',41,4,NULL,4),(249,NULL,_binary '\0','\"왜 하필 지금?\"',42,4,NULL,4),(250,NULL,_binary '\0','여자가 가방에서 신문을 꺼냈다.',43,4,NULL,4),(251,NULL,_binary '\0','부고란이 펼쳐져 있었다.',44,4,NULL,4),(252,NULL,_binary '\0','\"담임 선생님이 돌아가셨대요.\"',45,4,NULL,4),(253,NULL,_binary '\0','미란은 그제야 이해했다.',46,4,NULL,4),(254,NULL,_binary '\0','\"마지막 인사를 하려고 온 거구나.\"',47,4,NULL,4),(255,NULL,_binary '\0','여자가 일어났다.',48,4,NULL,4),(256,NULL,_binary '\0','\"선생님이 항상 말씀하셨어요. 신발 소리 나지 않게 조용히 다니라고.\"',49,4,NULL,4),(257,NULL,_binary '\0','\"지금도 그 습관이 남아 있어서.\"',50,4,NULL,4),(258,NULL,_binary '\0','미란이 빨간 구두를 주워왔다.',51,4,NULL,4),(259,NULL,_binary '\0','\"이거 신고 나가세요.\"',52,4,NULL,4),(260,NULL,_binary '\0','여자가 구두를 신더니 한 번 돌아봤다.',53,4,NULL,4),(261,NULL,_binary '\0','\"고마워요. 그리고 죄송해요.\"',54,4,NULL,4),(262,NULL,_binary '\0','정문이 열리고 닫히는 소리가 들렸다.',55,4,NULL,4),(263,NULL,_binary '\0','미란은 3학년 4반으로 돌아가서 칠판을 지웠다.',56,4,NULL,4),(264,NULL,_binary '\0','저는 23살 여자고요, 남자친구랑 사귄 지 6개월 됐어요.',1,5,NULL,5),(265,NULL,_binary '\0','남자친구가 제 꿈을 미리 알아요.',2,5,NULL,5),(266,NULL,_binary '\0','처음 설명할게요. 남자친구는 예술대학에서 심리학을 복수전공하는 애예요.',3,5,NULL,5),(267,NULL,_binary '\0','성격이 되게 신비로운 타입이거든요.',4,5,NULL,5),(268,NULL,_binary '\0','첫 만남에서부터 뭔가 특이했어요.',5,5,NULL,5),(269,NULL,_binary '\0','\"오늘 밤에 파란색 나비 꿈을 꿀 것 같은데요?\" 이렇게 말하더라고요.',6,5,NULL,5),(270,NULL,_binary '\0','저는 그냥 \"아 네...ㅋㅋㅋ\" 이랬거든요.',7,5,NULL,5),(271,NULL,_binary '\0','그런데 진짜로 그날 밤에 파란 나비가 나오는 꿈을 꿨어요.',8,5,NULL,5),(272,NULL,_binary '\0','우연이겠지 하고 넘어갔는데.',9,5,NULL,5),(273,NULL,_binary '\0','두 번째 만남에서 또 말하는 거예요.',10,5,NULL,5),(274,NULL,_binary '\0','\"혹시 어릴 때 살던 집 나오는 꿈 꾸지 않았어요?\"',11,5,NULL,5),(275,NULL,_binary '\0','맞았어요. 진짜 초등학교 때 살던 빌라가 나왔어요.',12,5,NULL,5),(276,NULL,_binary '\0','세 번째 만남에서는 \"계단에서 떨어지는 꿈 조심하세요\" 이러더라고요.',13,5,NULL,5),(277,NULL,_binary '\0','그날 밤에 진짜 꿈에서 계단을 굴러떨어졌어요.',14,5,NULL,5),(278,NULL,_binary '\0','이게 우연일까요?',15,5,NULL,5),(279,NULL,_binary '\0','사귀고 나서도 계속됐어요.',16,5,NULL,5),(280,NULL,_binary '\0','매일 자기 전에 카톡이 와요.',17,5,NULL,5),(281,NULL,_binary '\0','\"오늘은 물 관련 꿈일 것 같아\" \"검은 고양이 나올 거야\" 이런 식으로.',18,5,NULL,5),(282,NULL,_binary '\0','정확도가 80% 정도 돼요.',19,5,NULL,5),(283,NULL,_binary '\0','처음엔 신기했는데 점점 무서워지기 시작했어요.',20,5,NULL,5),(284,NULL,_binary '\0','어떻게 이런 게 가능한 거예요?',21,5,NULL,5),(285,NULL,_binary '\0','남자친구한테 직접 물어봤어요.',22,5,NULL,5),(286,NULL,_binary '\0','\"야 솔직히 말해. 너 뭐야? 초능력자야?\"',23,5,NULL,5),(287,NULL,_binary '\0','그랬더니 웃으면서 \"그냥 관찰하는 거야\" 이러더라고요.',24,5,NULL,5),(288,NULL,_binary '\0','\"관찰이 뭔데?\"',25,5,NULL,5),(289,NULL,_binary '\0','\"네가 하루 종일 뭘 했는지, 뭘 먹었는지, 무슨 영화 봤는지 들으면 꿈이 예측돼\"',26,5,NULL,5),(290,NULL,_binary '\0','\"심리학으로 배운 거야. 무의식이랑 꿈의 연관성 같은 거\"',27,5,NULL,5),(291,NULL,_binary '\0','그래도 이상했어요.',28,5,NULL,5),(292,NULL,_binary '\0','그런데 한 달 전에 충격적인 걸 발견했어요.',29,5,NULL,5),(293,NULL,_binary '\0','남자친구 집에 갔는데 책상에 노트가 있었어요.',30,5,NULL,5),(294,NULL,_binary '\0','제목이 \"○○이 꿈 패턴 분석\"이었어요. 제 이름이 들어간.',31,5,NULL,5),(295,NULL,_binary '\0','펼쳐보니까 날짜별로 제 하루 일과랑 그날 밤 꿈이 정리되어 있었어요.',32,5,NULL,5),(296,NULL,_binary '\0','\"3월 5일: 매운 음식 + 스트레스 → 쫓기는 꿈 (적중)\"',33,5,NULL,5),(297,NULL,_binary '\0','\"3월 12일: 옛날 사진 봄 + 향수 → 고향 관련 꿈 (적중)\"',34,5,NULL,5),(298,NULL,_binary '\0','이런 식으로 3개월치가 빼곡히 적혀있었어요.',35,5,NULL,5),(299,NULL,_binary '\0','그리고 맨 뒤 페이지에 \"꿈 조작 실험\"이라고 써 있었어요.',36,5,NULL,5),(300,NULL,_binary '\0','뭐냐면, 제가 특정 꿈을 꿀 만한 상황을 일부러 만드는 거였어요.',37,5,NULL,5),(301,NULL,_binary '\0','데이트할 때 일부러 파란색 옷을 입고 나비 장식품을 보여준다든지.',38,5,NULL,5),(302,NULL,_binary '\0','옛날 이야기를 꺼낸다든지.',39,5,NULL,5),(303,NULL,_binary '\0','제 꿈을 예측하는 게 아니라 조종하고 있었던 거예요.',40,5,NULL,5),(304,NULL,_binary '\0','남자친구한테 따졌더니 \"미안해, 근데 네 반응이 너무 귀여워서\" 이러더라고요.',41,5,NULL,5),(305,NULL,_binary '\0','\"그리고 이거 덕분에 네 심리 상태도 더 잘 알게 되고\"',42,5,NULL,5),(306,NULL,_binary '\0','\"나쁜 의도는 아니었어\"',43,5,NULL,5),(307,NULL,_binary '\0','이 사람 저를 실험용 쥐로 본 건가요?',44,5,NULL,5),(308,NULL,_binary '\0','근데 또 나쁜 사람은 아닌 것 같아요.',45,5,NULL,5),(309,NULL,_binary '\0','제가 스트레스받을 때 미리 알고 챙겨주거든요.',46,5,NULL,5),(310,NULL,_binary '\0','어떻게 해야 할까요...?',47,5,NULL,5),(311,NULL,_binary '\0','고3 여자친구랑 8개월 사귀고 있음.',1,6,NULL,6),(312,NULL,_binary '\0','내가 대학교 1학년이고 걔가 고등학교 3학년인데 동네 선후배 사이였음.',2,6,NULL,6),(313,NULL,_binary '\0','원래 공부 잘하는 애가 아니라서 부모님이 과외 선생님을 붙여줬다더라.',3,6,NULL,6),(314,NULL,_binary '\0','수학 과외인데 대학생 형이 온다고 함.',4,6,NULL,6),(315,NULL,_binary '\0','처음에는 그냥 좋았음. 성적이 올라가면 나도 뿌듯하니까.',5,6,NULL,6),(316,NULL,_binary '\0','근데 몇 주 지나더니 과외 얘기를 자주 하기 시작함.',6,6,NULL,6),(317,NULL,_binary '\0','\"오빠가 문제 이렇게 설명해줘서 이해했어\" 이런 식으로.',7,6,NULL,6),(318,NULL,_binary '\0','뭔가 밝아진 느낌? 원래는 공부 얘기만 하면 짜증내던 애가.',8,6,NULL,6),(319,NULL,_binary '\0','그래도 처음에는 공부에 흥미가 생긴 건가 싶었음.',9,6,NULL,6),(320,NULL,_binary '\0','근데 하루는 데이트하는데 문자가 와서 \"잠깐만\" 하면서 답장하는 거임.',10,6,NULL,6),(321,NULL,_binary '\0','누구냐고 물어봤더니 \"과외 선생님이 숙제 관련해서 물어봤어\" 이러더라.',11,6,NULL,6),(322,NULL,_binary '\0','주말에도 과외와 관련된 문자를 주고받나?',12,6,NULL,6),(323,NULL,_binary '\0','이상했지만 그냥 넘어갔음. 공부 열심히 하는 건 좋은 일이니까.',13,6,NULL,6),(324,NULL,_binary '\0','그런데 이번 주에 걔 친구를 만났는데.',14,6,NULL,6),(325,NULL,_binary '\0','\"요즘 과외 어때?\" 물어봤더니 친구가 뭔가 어색하게 웃더라.',15,6,NULL,6),(326,NULL,_binary '\0','\"그냥... 열심히 하는 것 같아\" 이러는데 말투가 이상함.',16,6,NULL,6),(327,NULL,_binary '\0','그래서 \"과외 선생님은 어떤 분이야?\" 물어봤더니.',17,6,NULL,6),(328,NULL,_binary '\0','친구가 잠깐 멈추더니 \"잘 모르겠어, 직접 물어봐\" 이러고 화제를 돌리더라고.',18,6,NULL,6),(329,NULL,_binary '\0','그날부터 신경이 쓰이기 시작함.',19,6,NULL,6),(330,NULL,_binary '\0','과외 하는 날을 유심히 보니까 평소보다 화장을 진하게 함.',20,6,NULL,6),(331,NULL,_binary '\0','원래는 민낯으로 다니는 애였는데.',21,6,NULL,6),(332,NULL,_binary '\0','그리고 과외 끝나고 연락하면 답장이 늦어짐.',22,6,NULL,6),(333,NULL,_binary '\0','\"피곤해서 잠깐 잤어\" 이런 핑계를 대는데.',23,6,NULL,6),(334,NULL,_binary '\0','어느 날 과외 하는 날에 집 앞에 가봤음.',24,6,NULL,6),(335,NULL,_binary '\0','걔 집 근처 카페에서 기다리면서 언제 나오나 봤거든.',25,6,NULL,6),(336,NULL,_binary '\0','과외는 보통 2시간 한다고 했는데 3시간이 넘어도 안 나와.',26,6,NULL,6),(337,NULL,_binary '\0','그래서 전화했더니 \"아 지금 좀 어려운 문제 풀고 있어서 조금 늦어질 것 같아\" 이러더라.',27,6,NULL,6),(338,NULL,_binary '\0','전화 끊고 30분 더 기다리니까 드디어 나옴.',28,6,NULL,6),(339,NULL,_binary '\0','그런데 과외 선생님도 같이 나오는 거임.',29,6,NULL,6),(340,NULL,_binary '\0','멀어서 정확히는 안 보이는데 둘이 뭔가 얘기하면서 웃고 있더라.',30,6,NULL,6),(341,NULL,_binary '\0','그리고 헤어지기 전에 잠깐 손을 마주쳤음. 악수인지 뭔지는 모르겠지만.',31,6,NULL,6),(342,NULL,_binary '\0','집에 와서 \"과외 어땠어?\" 물어봤더니 \"그냥 평소대로\" 이러는 거임.',32,6,NULL,6),(343,NULL,_binary '\0','\"오늘 좀 늦게 끝났네?\" 했더니 \"응, 모르는 게 많아서\" 이러더라.',33,6,NULL,6),(344,NULL,_binary '\0','그런데 목소리가 뭔가 들떠 있었음.',34,6,NULL,6),(345,NULL,_binary '\0','며칠 뒤에 걔 엄마한테 안부 인사차 전화했음.',35,6,NULL,6),(346,NULL,_binary '\0','\"과외 효과 있나요?\" 물어봤더니 뭔가 이상한 말씀을 하시더라.',36,6,NULL,6),(347,NULL,_binary '\0','\"요즘 공부보다는 다른 걸 배우는 것 같아요\" 이러시는 거임.',37,6,NULL,6),(348,NULL,_binary '\0','무슨 뜻이냐고 했더니 \"글쎄요, 성적은 그대로인데 기분이 좋아 보여요\" 이러시더라.',38,6,NULL,6),(349,NULL,_binary '\0','그래서 어제 과외 선생님 번호를 알아냈음.',39,6,NULL,6),(350,NULL,_binary '\0','걔한테 \"과외 선생님 연락처 좀 달라, 나도 과외받고 싶어\" 이렇게 말해서.',40,6,NULL,6),(351,NULL,_binary '\0','의심 안 하고 바로 줬는데.',41,6,NULL,6),(352,NULL,_binary '\0','전화해서 \"안녕하세요, 과외 받고 있는 학생 남자친구입니다\" 이렇게 시작했음.',42,6,NULL,6),(353,NULL,_binary '\0','그랬더니 목소리가 확 달라지더라. 뭔가 당황하는 느낌?',43,6,NULL,6),(354,NULL,_binary '\0','\"아... 네, 안녕하세요\" 이러는데 어색함.',44,6,NULL,6),(355,NULL,_binary '\0','\"혹시 시간 되실 때 한 번 뵙고 과외 방식에 대해 상담받을 수 있을까요?\" 했더니.',45,6,NULL,6),(356,NULL,_binary '\0','\"그게... 요즘 좀 바빠서... 나중에 연락드릴게요\" 이러고 전화를 끊어버림.',46,6,NULL,6),(357,NULL,_binary '\0','보통 과외 선생님이면 학부모나 지인이 연락하면 반가워해야 하는 거 아님?',47,6,NULL,6),(358,NULL,_binary '\0','지금 여자친구한테 말해야 할지 그냥 넘어가야 할지 모르겠음.',48,6,NULL,6),(359,NULL,_binary '\0','확실한 건 아무것도 없는데 괜히 의심하는 건가 싶기도 하고.',49,6,NULL,6),(360,NULL,_binary '\0','근데 뭔가 이상한 건 분명함.',50,6,NULL,6);
/*!40000 ALTER TABLE `script` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `script_option`
--

DROP TABLE IF EXISTS `script_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `script_option` (
  `option_id` bigint NOT NULL AUTO_INCREMENT,
  `ending_score` int DEFAULT NULL,
  `option_text` varchar(500) DEFAULT NULL,
  `next_script_id` bigint DEFAULT NULL,
  `script_id` bigint NOT NULL,
  PRIMARY KEY (`option_id`),
  KEY `FKgepisfstw30ttchctvjykl1fk` (`next_script_id`),
  KEY `FKaysdv5ien4y0plo0qmkdwcj41` (`script_id`),
  CONSTRAINT `FKaysdv5ien4y0plo0qmkdwcj41` FOREIGN KEY (`script_id`) REFERENCES `script` (`script_id`),
  CONSTRAINT `FKgepisfstw30ttchctvjykl1fk` FOREIGN KEY (`next_script_id`) REFERENCES `script` (`script_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `script_option`
--

LOCK TABLES `script_option` WRITE;
/*!40000 ALTER TABLE `script_option` DISABLE KEYS */;
/*!40000 ALTER TABLE `script_option` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_asset`
--

DROP TABLE IF EXISTS `user_asset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_asset` (
  `asset_id` bigint NOT NULL AUTO_INCREMENT,
  `current_points` int DEFAULT NULL,
  `streak_days` int DEFAULT NULL,
  `total_exp` int DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`asset_id`),
  UNIQUE KEY `UKhb7y0msn68vxffcsprm86y9l5` (`user_id`),
  CONSTRAINT `FK2swjam788mhmfjd6e3iiv1xg8` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_asset`
--

LOCK TABLES `user_asset` WRITE;
/*!40000 ALTER TABLE `user_asset` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_asset` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_progress`
--

DROP TABLE IF EXISTS `user_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_progress` (
  `progress_id` bigint NOT NULL AUTO_INCREMENT,
  `progress_rate` int DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `content_id` bigint NOT NULL,
  `last_content_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `is_attained` bit(1) DEFAULT b'0',
  `achieve_date` date DEFAULT NULL,
  `completed_content_id` bigint DEFAULT NULL,
  `used_hints_count` int DEFAULT '0',
  `last_chapter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`progress_id`),
  KEY `FKgy8w0vyjc0chyh1k3fqvm58mb` (`content_id`),
  KEY `FKrt37sneeps21829cuqetjm5ye` (`user_id`),
  KEY `fk_progress_last_content` (`last_content_id`),
  KEY `FKdpmojrcfahqsg4eur8o7c9ydl` (`last_chapter_id`),
  CONSTRAINT `fk_progress_last_content` FOREIGN KEY (`last_content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKdpmojrcfahqsg4eur8o7c9ydl` FOREIGN KEY (`last_chapter_id`) REFERENCES `chapter` (`chapter_id`),
  CONSTRAINT `FKgy8w0vyjc0chyh1k3fqvm58mb` FOREIGN KEY (`content_id`) REFERENCES `content` (`content_id`),
  CONSTRAINT `FKrt37sneeps21829cuqetjm5ye` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_progress`
--

LOCK TABLES `user_progress` WRITE;
/*!40000 ALTER TABLE `user_progress` DISABLE KEYS */;
INSERT INTO `user_progress` VALUES (2,100,'2026-05-31 20:15:53.630904',2,NULL,1,_binary '\0',NULL,NULL,0,NULL),(3,100,'2026-05-31 20:24:23.658829',3,NULL,1,_binary '\0',NULL,NULL,0,NULL),(4,100,'2026-05-31 20:37:00.715244',1,NULL,2,_binary '\0',NULL,NULL,0,NULL),(5,100,'2026-05-31 20:44:57.071416',2,NULL,2,_binary '\0',NULL,NULL,0,NULL),(6,100,'2026-05-31 20:50:47.611589',3,NULL,2,_binary '\0',NULL,NULL,0,NULL),(7,100,'2026-05-31 20:58:52.803805',4,NULL,2,_binary '\0',NULL,NULL,0,NULL),(8,100,'2026-05-31 21:08:26.975757',5,NULL,2,_binary '\0',NULL,NULL,0,NULL),(10,100,'2026-06-01 20:08:19.153875',3,NULL,3,_binary '\0',NULL,NULL,0,NULL),(11,100,'2026-06-02 15:13:18.958743',1,NULL,3,_binary '\0',NULL,NULL,0,NULL),(12,100,'2026-06-02 15:25:57.158355',4,NULL,3,_binary '\0',NULL,NULL,0,NULL);
/*!40000 ALTER TABLE `user_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_study_log`
--

DROP TABLE IF EXISTS `user_study_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_study_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  `lang_code` varchar(5) DEFAULT NULL,
  `user_response` text,
  `quiz_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`log_id`),
  KEY `FKrf8vxohbg12yki1e4r46jyymo` (`quiz_id`),
  KEY `FKkusbp9o9unoh1cva7gohhanje` (`user_id`),
  CONSTRAINT `FKkusbp9o9unoh1cva7gohhanje` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKrf8vxohbg12yki1e4r46jyymo` FOREIGN KEY (`quiz_id`) REFERENCES `quiz_detail` (`quiz_id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_study_log`
--

LOCK TABLES `user_study_log` WRITE;
/*!40000 ALTER TABLE `user_study_log` DISABLE KEYS */;
INSERT INTO `user_study_log` VALUES (51,'2026-06-01 20:08:19.091371',_binary '','nervous',31,3),(52,'2026-06-01 20:08:19.102988',_binary '','shake',34,3),(53,'2026-06-01 20:08:19.114375',_binary '','shocked',37,3),(54,'2026-06-01 20:08:19.129839',_binary '','empty',40,3),(55,'2026-06-01 20:08:19.145334',_binary '','ring',43,3),(56,'2026-06-02 15:13:18.834748',_binary '','annoyed',1,3),(57,'2026-06-02 15:13:18.889316',_binary '','curiosity',4,3),(58,'2026-06-02 15:13:18.905319',_binary '','courage',7,3),(59,'2026-06-02 15:13:18.924477',_binary '','empty',10,3),(60,'2026-06-02 15:13:18.942315',_binary '','occasionally',13,3),(61,'2026-06-02 15:25:57.072640',_binary '','look around',46,3),(62,'2026-06-02 15:25:57.095445',_binary '','empty',49,3),(63,'2026-06-02 15:25:57.120331',_binary '','quiet',52,3),(64,'2026-06-02 15:25:57.135812',_binary '','realize',55,3),(65,'2026-06-02 15:25:57.149342',_binary '','erase',58,3);
/*!40000 ALTER TABLE `user_study_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `auth_provider` varchar(20) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `email_verified` bit(1) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `social_id` varchar(100) DEFAULT NULL,
  `continuous_streak_days` int DEFAULT '0',
  `create_at` datetime(6) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `story_nickname` varchar(50) DEFAULT NULL,
  `language` varchar(10) DEFAULT NULL,
  `level_code` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'LOCAL','parksoo0206@naver.com',_binary '',NULL,'$2a$10$tIrYLICLh/a9/ZiAda1I/eEMxmLbDkmwFI948TL9I6Zw3Hqx9ChY.',NULL,0,'2026-05-31 16:38:09.272200','박수현',NULL,'EN',1),(2,'LOCAL','dahyunche@gmail.com',_binary '',NULL,'$2a$10$XaEhQVmWgLuV9Kafy2FarO8AnEZqiGKhyjJJHyk9J2Xhptf/yWKYq',NULL,0,'2026-05-31 20:26:51.804287','채다현',NULL,'EN',2),(3,'LOCAL','duddnkim1212@gmail.com',_binary '',NULL,'$2a$10$PVyPo10tO13A32QcC4D1MeaU45GcXnBC/CavTkENjddPnxo9WiSmK',NULL,0,'2026-06-01 19:27:54.289083','duddn02',NULL,'EN',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-02  9:44:52
