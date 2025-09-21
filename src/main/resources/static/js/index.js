document.addEventListener('DOMContentLoaded', function () {
    const jobSelect = document.getElementById('jobSelect');
    if (jobSelect) {
        jobSelect.addEventListener('change', function () {
            const job = this.value;
            window.location.href = '/go?job=' + encodeURIComponent(job);
        });
    }
});