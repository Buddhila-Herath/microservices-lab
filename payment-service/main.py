from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import os
import uvicorn

app = FastAPI()

class Payment(BaseModel):
    id: Optional[int] = None
    orderId: int
    amount: float
    method: str
    status: str = "SUCCESS"

payments = []
id_counter = 1

# Health check
@app.get("/health")
def health():
    return {"status": "UP"}

@app.get("/payments", response_model=List[Payment])
def get_payments():
    return payments

@app.post("/payments/process", response_model=Payment, status_code=201)
def process_payment(payment: Payment):
    global id_counter
    if payment.amount <= 0:
        raise HTTPException(status_code=400, detail="Amount must be positive")
    payment.id = id_counter
    id_counter += 1
    payment.status = "SUCCESS"
    payments.append(payment)
    return payment

@app.get("/payments/{payment_id}", response_model=Payment)
def get_payment(payment_id: int):
    for p in payments:
        if p.id == payment_id:
            return p
    raise HTTPException(status_code=404, detail="Payment not found")

if __name__ == "__main__":
    port = int(os.getenv("PORT", 8083))
    uvicorn.run(app, host="0.0.0.0", port=port)
