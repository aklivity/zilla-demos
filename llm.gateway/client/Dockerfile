FROM python:3.13-slim

ENV GRADIO_ANALYTICS_ENABLED=False \
    PYTHONUNBUFFERED=1

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY *.py .

EXPOSE 7860
CMD ["python", "app.py"]
