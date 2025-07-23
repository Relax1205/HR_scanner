document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('fileInput');
    const uploadArea = document.getElementById('uploadArea');
    const loading = document.getElementById('loading');
    const resultArea = document.getElementById('resultArea');
    const progressBar = document.getElementById('progressBar');
    const historyList = document.getElementById('historyList');
    const jobSelect = document.getElementById('jobSelect');
    const jobDescription = document.getElementById('jobDescription');

    const jobTitles = {
        analyst: 'Аналитик данных (DATA SCIENTIST, ML Engineer)',
        engineer: 'Инженер данных (DATA ENGINEER)',
        designer: 'Технический аналитик в ИИ (TECHNICAL ANALYST IN AI)',
        manager: 'Менеджер в ИИ (MANAGER IN AI)'
    };

    const descriptions = {
        analyst: `
            <strong>Аналитик данных</strong>
            (Data scientist, ml engineer)<br>
            Специалист, который работает с данными компании, анализирует их и разрабатывает решения на основе ИИ.<br>
            Совместно с техническими аналитиками формирует технические метрики, которые зависят от бизнес-метрик.
        `,
        data_engineer: `
            <strong>Инженер данных</strong>
            (Data engineer)<br>
            Специалист, который отвечает за сбор, анализ, очистку и подготовку данных для последующего 
            использования. Работает с системами хранения и анализа данных, обеспечивая их эффективное 
            функционирование, а также поддержку систем версионирования данных.
        `,
        tech_analyst: `
            <strong>Технический аналитик в ИИ</strong>
            (Technical analyst in ai)<br>
            Специалист, который обеспечивает эффективное взаимодействие между аналитиком данных и заказчиком.
            Анализирует потребности бизнеса, подтверждает и уточняет проблематику, анализирует бизнес-процессы.
        `,
        manager: `
            <strong>Менеджер в ИИ</strong>
            (Manager in ai)<br>
            Специалист, который обеспечивает общее выполнение проекта, работу по бюджету, ресурсам и срокам.
            Отвечает за конверсию и вывод решений в продуктив на организационном уровне.
        `
    };

    function updateJobDescription(selectedValue) {
        if (selectedValue && descriptions[selectedValue]) {
            jobDescription.innerHTML = descriptions[selectedValue];
        } else {
            jobDescription.innerHTML = 'Описание недоступно.';
        }
    }

    const initialSelectedValue = jobSelect.value;
    updateJobDescription(initialSelectedValue);

    jobSelect.addEventListener('change', (event) => {
        const selectedValue = event.target.value;
        updateJobDescription(selectedValue);
    });

    fileInput.addEventListener('change', function (e) {
        if (e.target.files.length) {
            processFile(e.target.files[0]);
        }
    });

    uploadArea.addEventListener('dragover', function (e) {
        e.preventDefault();
        uploadArea.style.backgroundColor = '#e6f2ff';
    });

    uploadArea.addEventListener('dragleave', function () {
        uploadArea.style.backgroundColor = 'white';
    });

    uploadArea.addEventListener('drop', function (e) {
        e.preventDefault();
        uploadArea.style.backgroundColor = 'white';

        if (e.dataTransfer.files.length) {
            processFile(e.dataTransfer.files[0]);
        }
    });

    async function processFile(file) {
        const allowedTypes = [
            'application/pdf',
            'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        ];

        if (!allowedTypes.includes(file.type)) {
            alert('Пожалуйста, загрузите файл в формате PDF, DOC или DOCX');
            return;
        }

        loading.style.display = 'block';
        resultArea.style.display = 'none';
        resultArea.innerHTML = '';

        let progress = 0;
        const progressInterval = setInterval(() => {
            progress += 5;
            progressBar.style.width = `${progress}%`;
            if (progress >= 100) clearInterval(progressInterval);
        }, 200);

        try {
            const formData = new FormData();
            formData.append('resume', file);
            formData.append('job', jobSelect.value);

            const response = await fetch('http://localhost:3001/api/analyze', {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Ошибка анализа');
            }

            const result = await response.json();
            console.log('Received analysis:', result);

            loading.style.display = 'none';
            resultArea.style.display = 'block';
            
            const formattedSummary = formatAnalysisResult(result.summary, result.matchPercentage);
            
            resultArea.innerHTML = `
                <h3>Результат анализа резюме "${file.name}"</h3>
                <h4>Вакансия: ${jobTitles[result.job] || result.job}</h4>
                <div class="analysis-result">
                    ${formattedSummary}
                </div>
            `;

            addToHistory(file.name, result.matchPercentage, result.job);

        } catch (error) {
            console.error('Error:', error);
            loading.style.display = 'none';
            resultArea.style.display = 'block';
            resultArea.innerHTML = `
                <h3>Ошибка</h3>
                <p>${error.message}</p>
            `;
        } finally {
            clearInterval(progressInterval);
            progressBar.style.width = '0%';
        }
    }

    function formatAnalysisResult(text, percentage) {
        // Улучшенное форматирование результатов
        return text
            .replace(/### (.*?):/g, '<h4>$1:</h4>')
            .replace(/\n/g, '<br>')
            .replace(/(\d+%)/g, '<span class="percentage">$1</span>')
            .replace(/Соответствие:/g, '<strong>Соответствие:</strong>')
            .replace(/Уровень \d/g, match => `<span class="skill-level">${match}</span>`)
            .replace(/Да/g, '<span class="match-yes">Да</span>')
            .replace(/Нет/g, '<span class="match-no">Нет</span>');
    }

    function addToHistory(filename, percentage, job) {
        const newHistoryItem = document.createElement('li');
        newHistoryItem.innerHTML = `
            <span class="history-number">${historyList.children.length + 1}.</span>
            <span class="history-filename">${filename}</span>
            <span class="history-job">${jobTitles[job] || job}</span>
            <span class="history-percentage" style="color: ${getPercentageColor(percentage)}">
                ${percentage}%
            </span>
        `;
        historyList.appendChild(newHistoryItem);
    }

    function getPercentageColor(percentage) {
        if (percentage >= 70) return '#4CAF50';
        if (percentage >= 40) return '#FFC107';
        return '#F44336';
    }


});