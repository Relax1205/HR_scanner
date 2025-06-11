require('dotenv').config();
const express = require('express');
const fileUpload = require('express-fileupload');
const axios = require('axios');
const cors = require('cors');
const pdf = require('pdf-parse');
const mammoth = require('mammoth');
const path = require('path');
const fs = require('fs');

const app = express();

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

app.use(cors({
  origin: 'http://localhost:3001',
  credentials: true
}));

app.use(fileUpload({
  limits: { fileSize: 10 * 1024 * 1024 },
  abortOnLimit: true,
  useTempFiles: true,
  tempFileDir: path.join(__dirname, 'temp')
}));

app.use(express.static(path.join(__dirname, '../frontend')));

const OPENROUTER_API_KEY = process.env.OPENROUTER_API_KEY || 'your-api-key-here';

const apiClient = axios.create({
  baseURL: 'https://openrouter.ai/api/v1',
  timeout: 45000,
  headers: {
    'Authorization': `Bearer ${OPENROUTER_API_KEY}`,
    'HTTP-Referer': 'http://localhost:3001',
    'X-Title': 'HR Scanner'
  }
});

// Матрица компетенций для всех вакансий
const COMPETENCE_MATRIX = {
  analyst: {
    'Общие компетенции': {
      'Определения, история развития и главные тренды ИИ': 1,
      'Процесс, стадии и методологии разработки решений (Docker, Linux/Bash, Git)': 2,
      'Статистические методы и первичный анализ данных': 2
    },
    'Инструменты': {
      'Оценка качества работы методов ИИ': 2,
      'Языки программирования и библиотеки (Python, С++)': 2
    },
    'Алгоритмы и методы ИИ': {
      'Методы машинного обучения': 2,
      'Методы оптимизации': 2,
      'Анализ изображений и видео': 2,
      'Анализ естественного языка': 2,
      'Основы глубокого обучения': 2,
      'Глубокое обучение для анализа изображений/видео': 2,
      'Глубокое обучение для анализа текста': 2,
      'Обучение с подкреплением': 1
    },
    'Работа с данными': {
      'SQL базы данных (GreenPlum, Postgres, Oracle)': 1,
      'NoSQL базы данных (Cassandra, MongoDB, ElasticSearch)': 1,
      'Качество и предобработка данных': 2
    }
  },
  engineer: {
    'Общие компетенции': {
      'Определения, история развития и главные тренды ИИ': 1,
      'Процесс, стадии и методологии разработки решений': 2
    },
    'Инструменты': {
      'Языки программирования и библиотеки (Python, С++)': 2
    },
    'Алгоритмы и методы ИИ': {
      'Массово параллельные вычисления (GPU)': 1,
      'Работа с распределенными системами': 2
    },
    'Работа с данными': {
      'Машинное обучение на больших данных': 2,
      'Потоковая обработка данных': 2,
      'SQL базы данных': 3,
      'NoSQL базы данных': 3,
      'Массово параллельная обработка данных': 2,
      'Hadoop, SPARK, Hive': 2,
      'Качество и предобработка данных': 3
    }
  },
  designer: {
    'Общие компетенции': {
      'Определения, история развития и главные тренды ИИ': 1,
      'Процесс, стадии и методологии разработки решений': 1
    },
    'Алгоритмы и методы ИИ': {
      'Рекомендательные системы': 1,
      'Анализ изображений и видео': 1,
      'Анализ естественного языка': 1,
      'Основы глубокого обучения': 1
    },
    'Работа с данными': {
      'SQL базы данных': 1,
      'NoSQL базы данных': 1,
      'Качество и предобработка данных': 1
    }
  },
  manager: {
    'Общие компетенции': {
      'Определения, история развития и главные тренды ИИ': 1,
      'Процесс, стадии и методологии разработки решений': 1
    },
    'Инструменты': {
      'Оценка качества работы методов ИИ': 1
    },
    'Алгоритмы и методы ИИ': {
      'Анализ изображений и видео': 1,
      'Анализ естественного языка': 1
    },
    'Работа с данными': {
      'SQL базы данных': 1,
      'NoSQL базы данных': 1,
      'Качество и предобработка данных': 1
    }
  }
};

async function extractText(file) {
  try {
    const tempPath = path.join(__dirname, 'temp', `${Date.now()}_${file.name}`);
    await file.mv(tempPath);
    
    if (file.mimetype === 'application/pdf') {
      const dataBuffer = fs.readFileSync(tempPath);
      const data = await pdf(dataBuffer);
      fs.unlinkSync(tempPath);
      return data.text;
    } else if (file.mimetype.includes('msword') || file.mimetype.includes('wordprocessingml')) {
      const result = await mammoth.extractRawText({ path: tempPath });
      fs.unlinkSync(tempPath);
      return result.value;
    }
    throw new Error('Unsupported file type');
  } catch (err) {
    console.error('Detailed extraction error:', err);
    throw new Error(`Failed to extract text: ${err.message}`);
  }
}

app.post('/api/analyze', async (req, res) => {
  try {
    if (!req.files?.resume) {
      return res.status(400).json({ error: 'No file uploaded' });
    }

    const resume = req.files.resume;
    const job = req.body.job || 'analyst';
    console.log('Processing file:', resume.name, 'for job:', job);

    const textContent = await extractText(resume);
    if (!textContent) {
      throw new Error('No text content extracted');
    }

    const jobRequirements = COMPETENCE_MATRIX[job];
    const requirementsText = Object.entries(jobRequirements)
      .map(([category, skills]) => {
        return `### ${category}:\n${Object.entries(skills)
          .map(([skill, level]) => `- ${skill}: требуется уровень ${level}`)
          .join('\n')}`;
      })
      .join('\n\n');

    const response = await apiClient.post('/chat/completions', {
      model: "deepseek/deepseek-v3-base:free",
      messages: [
        {
          role: "system",
          content: `Ты HR-эксперт. Проанализируй резюме и оцени соответствие вакансии по следующим критериям:

Требуемые компетенции для вакансии:
${requirementsText}

Формат анализа:
1. Для каждой компетенции укажи:
   - Найденный уровень (1-3)
   - Подтверждающие фразы из резюме
   - Соответствие требованию (Да/Нет)

2. Рассчитай общий процент соответствия

3. Выведи структурированный отчет в формате:
### Ключевая информация:
[анализ резюме]

### Оценка компетенций:
[таблица с оценками]

### Итоговый результат:
[процент]% соответствия`
        },
        {
          role: "user",
          content: `Резюме кандидата на вакансию ${job}:\n\n${textContent.substring(0, 5000)}`
        }
      ],
      max_tokens: 3000,
      temperature: 0.3
    });

    console.log('API Response:', response.data); // Логируем полный ответ

    if (!response.data || !response.data.choices || !response.data.choices[0] || !response.data.choices[0].message) {
      console.error('Invalid API response:', response.data);
      throw new Error('Invalid API response structure');
    }

    const result = response.data.choices[0].message.content;
    console.log('Analysis result:', result);

    // Улучшенное извлечение процента
    const percentageMatch = result.match(/Итоговый результат:\s*(\d+)%/);
    const matchPercentage = percentageMatch ? parseInt(percentageMatch[1]) : 
                          result.match(/(\d+)% соответствия/) ? parseInt(result.match(/(\d+)% соответствия/)[1]) : 0;

    res.json({
      success: true,
      summary: result,
      matchPercentage: matchPercentage,
      job: job,
      rawResponse: response.data // Для отладки
    });

  } catch (error) {
    console.error('Full error:', error);
    res.status(500).json({ 
      success: false,
      error: error.message,
      stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
    });
  }
});

function extractPercentage(text) {
  const percentageMatch = text.match(/(\d+)%/);
  return percentageMatch ? parseInt(percentageMatch[1]) : null;
}

if (!fs.existsSync(path.join(__dirname, 'temp'))) {
  fs.mkdirSync(path.join(__dirname, 'temp'));
}

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../frontend/index.html'));
});

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});