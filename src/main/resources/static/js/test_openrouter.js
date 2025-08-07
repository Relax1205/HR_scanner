const fetch = require('node-fetch');
const OPENROUTER_API_KEY = process.env.OPENROUTER_API_KEY || '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'; // Вставь свой ключ или используй .env

(async () => {
  const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${OPENROUTER_API_KEY}`,
      "Content-Type": "application/json",
      "HTTP-Referer": "http://localhost:3001",
      "X-Title": "HR Scanner"
    },
    body: JSON.stringify({
      "model": "deepseek/deepseek-chat-v3-0324:free",
      "messages": [
        {
          "role": "user",
          "content": "What is the meaning of life?"
        }
      ]
    })
  });
  const text = await response.text();
  console.log('Status:', response.status);
  console.log('Headers:', response.headers.raw());
  console.log('Body:', text);
})();