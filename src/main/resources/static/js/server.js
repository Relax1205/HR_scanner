const fetch = require('node-fetch'); // npm install node-fetch@2
require('dotenv').config();
const express = require('express');
const fileUpload = require('express-fileupload');
const cors = require('cors');
const pdf = require('pdf-parse');
const mammoth = require('mammoth');
const path = require('path');
const fs = require('fs');

const app = express();

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

app.use(cors());

app.use(fileUpload({
  limits: { fileSize: 10 * 1024 * 1024 },
  abortOnLimit: true,
  useTempFiles: true,
  tempFileDir: path.join(__dirname, 'temp')
}));

const OPENROUTER_API_KEY = process.env.OPENROUTER_API_KEY;

async function askOpenRouter(prompt) {
  const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${OPENROUTER_API_KEY}`,
      "HTTP-Referer": "http://localhost:3001",
      "X-Title": "HR Scanner",
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      "model": "deepseek/deepseek-chat-v3-0324:free",
      "messages": [
        {
          "role": "system",
          "content": "Ты — HR-бот, возвращай только JSON, без пояснений."
        },
        {
          "role": "user",
          "content": prompt
        }
      ]
    })
  });
  const text = await response.text();
  if (!response.ok) {
    console.error('OpenRouter error response:', text);
    throw new Error(`OpenRouter error: ${response.status} ${text}`);
  }
  try {
    return JSON.parse(text);
  } catch (e) {
    console.error('OpenRouter returned non-JSON:', text);
    throw new Error('OpenRouter did not return JSON. See server log for details.');
  }
}

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
    console.log('POST /api/analyze called');
    if (!req.files?.resume) {
      console.log('No file uploaded');
      return res.status(400).json({ error: 'No file uploaded' });
    }
    if (!OPENROUTER_API_KEY || OPENROUTER_API_KEY === 'your-api-key-here' || OPENROUTER_API_KEY.trim() === '') {
      return res.status(500).json({ error: 'OpenRouter API ключ не указан или невалиден. Укажите рабочий ключ в .env (OPENROUTER_API_KEY=sk-...)' });
    }
    const resume = req.files.resume;
    const job = req.body.job || 'analyst';
    console.log('File received:', resume.name, 'Job:', job);
    const textContent = await extractText(resume);
    if (!textContent) {
      throw new Error('No text content extracted');
    }
    let shortResume = textContent.substring(0, 2000);
    const prompt = `Ты — HR-бот. Проанализируй резюме кандидата на вакансию ${job}. Верни только валидный JSON строго в формате:\n{\n  \"name\": <имя кандидата>,\n  \"job\": <название вакансии>,\n  \"persent\": <процент соответствия резюме вакансии>,\n  \"skills\": [ {\"skill\": <название навыка>} ... ],\n  \"experience\": <краткое описание опыта работы или null>,\n  \"education\": <краткое описание образования или null>\n}\nНе используй markdown-обёртку (никаких тройных кавычек, никаких \`\`\`json и \`\`\`), только чистый JSON без пояснений.\nЕсли не можешь найти имя, опыт или образование — пиши null или пустой массив.\nРезюме:\n${shortResume}`;
    const openRouterResult = await askOpenRouter(prompt);
    let content = openRouterResult.choices?.[0]?.message?.content;
    if (content) {
      // Удаляем markdown-обёртку, если вдруг она всё же есть
      content = content.replace(/```json|```/g, '').trim();
    }
    let result;
    try {
      result = JSON.parse(content);
    } catch (e) {
      console.error('OpenRouter вернул невалидный JSON:', content);
      throw new Error('OpenRouter вернул невалидный JSON: ' + content);
    }
    console.log('Parsed result:', result);
    // Сохраняем результат в data.json
    const outPath = path.join(__dirname, '../json_results/data.json');
    fs.mkdirSync(path.dirname(outPath), { recursive: true });
    fs.writeFileSync(outPath, JSON.stringify(result, null, 2), 'utf-8');
    console.log('Result saved to', outPath);
    res.json(result);
    console.log('Response sent to client');
  } catch (error) {
    console.error('Full error:', error);
    res.status(500).json({ error: error.message });
  }
});

if (!fs.existsSync(path.join(__dirname, 'temp'))) {
  fs.mkdirSync(path.join(__dirname, 'temp'));
}

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});

app.get('/', (req, res) => {
  res.send('HR Scanner backend is running!');
});
