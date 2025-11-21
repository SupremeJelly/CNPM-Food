FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app.py .
ENV PYTHONUNBUFFERED=1
ENV METRICS_PORT=9118
EXPOSE 9118
CMD ["python", "app.py"]
