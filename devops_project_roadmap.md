# Lộ trình Đạt Điểm Tối Đa (10/10) - Đồ án 2: Xây Dựng Hệ Thống CD

Chúc mừng nhóm bạn đã triển khai thành công K3s, Istio, PostgreSQL Operator và đưa service **Cart** lên trạng thái **READY 2/2** (trạng thái `2/2` chứng tỏ Istio sidecar injection đã hoạt động thành công trên namespace `yas`).

Dựa trên tài liệu yêu cầu đồ án, dưới đây là phân tích chi tiết và các việc cần làm tiếp theo để đạt điểm tối đa.

---

## I. Trạng Thế Hiện Tại & Đánh Giá Thuận Lợi
* **Đã Đạt:** K3s hoạt động, PostgreSQL Operator đã chạy thành công ở namespace `postgres`, `cart` service đã kết nối được DB ở namespace `yas`.
* **Thuận Lợi Lớn:** Pod `cart` chạy với `READY 2/2` $\rightarrow$ Bạn đã cấu hình thành công việc tự động tiêm (inject) Istio Sidecar (`istio-proxy`) vào namespace `yas`. Đây là tiền đề rất tốt để ăn trọn **2 điểm nâng cao cho Service Mesh**.

---

## II. Các Việc Cần Làm Để Đạt Điểm Tối Đa

### 1. Phần CI: Đổi Tag Image Sang Commit SHA (Yêu cầu 3 - Phần Cơ bản)
* **Yêu cầu:** Với mỗi branch của user tạo, khi commit code, hệ thống phải build image với **tag là commit ID cuối cùng** của branch đó và đẩy lên Docker Hub.
* **Hiện tại:** Pipeline `cart-ci.yaml` của bạn đang để tag cứng là `:test`.
* **Giải pháp:** Cần sửa bước build/push Docker trong các file CI (ví dụ: `cart-ci.yaml`) sử dụng biến có sẵn của GitHub Actions: `${{ github.sha }}` hoặc một chuỗi short SHA.
  * *Ví dụ trong `cart-ci.yaml`:*
    ```yaml
    tags: ${{ secrets.DOCKER_USERNAME }}/yas-cart:${{ github.sha }}
    ```

---

### 2. Phần CD: Tạo Job CD `developer_build` Truyền Tham Số (Yêu cầu 4 & 5 - Phần Cơ bản)
* **Yêu cầu:** 
  1. Tạo một job CD (ở đây ta dùng tính năng `workflow_dispatch` của GitHub Actions) cho phép developer chọn chạy thủ công.
  2. Input parameter gồm: Tên branch muốn deploy cho một service cụ thể (ví dụ: `dev_tax_service`).
  3. Khi chạy: Service được chọn sẽ được deploy bằng image có tag là commit ID của branch đó, còn các service khác giữ nguyên mặc định là tag `main` hoặc `latest`.
  4. Tạo job CD để **xóa** phần triển khai thử nghiệm này khi test xong (Yêu cầu 5).

* **Giải pháp cấu hình GitHub Actions (`developer-build.yaml`):**
  Sử dụng `workflow_dispatch` với đầu vào `inputs`:
  ```yaml
  on:
    workflow_dispatch:
      inputs:
        service_name:
          description: 'Tên microservice muốn deploy (vd: cart, product, tax...)'
          required: true
          default: 'cart'
        branch_name:
          description: 'Tên nhánh của service cần test thử'
          required: true
          default: 'feature/test-cd-cart'
  ```
  Trong workflow, ta sẽ:
  1. Check out nhánh `branch_name` của `service_name` được truyền vào.
  2. Lấy commit SHA cuối cùng của nhánh đó.
  3. Build & push image với tag là commit SHA đó.
  4. Chạy lệnh Helm để deploy riêng service đó với tag commit SHA mới vào Kubernetes. Các service còn lại vẫn deploy bằng tag `latest` từ nhánh `main`.

---

### 3. Phần Nâng Cao 1: Sử Dụng ArgoCD Cho Môi Trường `dev` & `staging` (+2 Điểm)
* **Yêu cầu:** 
  - `dev` namespace: Tự động deploy mỗi khi nhánh `main` thay đổi.
  - `staging` namespace: Chỉ deploy khi có sự kiện đóng tag release (ví dụ `v1.2.3`).
* **Giải pháp:**
  1. **Cài đặt ArgoCD** lên K3s:
     ```bash
     kubectl create namespace argocd
     kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
     ```
  2. **Tạo 2 Application trong ArgoCD**:
     * **yas-dev Application**: Trỏ vào thư mục Helm chart của dự án, target namespace là `dev`. Bật tính năng **Auto-Sync** từ nhánh `main`.
     * **yas-staging Application**: Target namespace là `staging`. Cấu hình đồng bộ từ Helm Chart nhưng target revision là tag mong muốn (ví dụ: `v*` hoặc cấu hình thủ công qua GitOps repository).

---

### 4. Phần Nâng Cao 2: Cấu Hình Istio Service Mesh (+2 Điểm)
Vì bạn đã cài đặt Istio thành công, phần này là cơ hội lớn để lấy trọn 2 điểm. Các bước cụ thể cần triển khai:

#### A. Kích Hoạt mTLS STRICT cho Namespace `yas` và `postgres`
Tạo file manifest `peer-authentication.yaml` để áp dụng chính sách mTLS bắt buộc (STRICT):
```yaml
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: yas
spec:
  mtls:
    mode: STRICT
```
*(Lưu ý: Làm tương tự cho namespace `postgres` hoặc cấu hình global toàn cụm mesh).*

#### B. Cấu Hình Tự Động Retry Khi Gặp Lỗi 500 (Retry Policy)
Cấu hình trong đối tượng `VirtualService` của Istio. Ví dụ cấu hình cho `cart-service`:
```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: cart
  namespace: yas
spec:
  hosts:
  - cart
  http:
  - route:
    - destination:
        host: cart
    retries:
      attempts: 3
      perTryTimeout: 2s
      retryOn: "5xx,connect-failure,refused-stream"
```

#### C. Thiết Lập Chính Sách Phân Quyền (Authorization Policy)
Đảm bảo các service chỉ được phép giao tiếp theo luồng thiết kế (Zero Trust).
*Ví dụ:* Chỉ cho phép `gateway` hoặc `bff` gọi tới `cart` service. Chặn tất cả các kết nối từ các pod không liên quan:
```yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: cart-auth-policy
  namespace: yas
spec:
  selector:
    matchLabels:
      app: cart
  action: ALLOW
  rules:
  - from:
    - source:
        principals: ["cluster.local/ns/yas/sa/storefront-bff", "cluster.local/ns/yas/sa/backoffice-bff"]
```
*(Lưu ý: Cần tạo ServiceAccount tương ứng cho các pod để Istio nhận diện được Identity).*

#### D. Trực Quan Hóa Với Kiali
1. Triển khai Kiali addon của Istio trên K3s VM.
2. Thực hiện gọi API (traffic generation) qua hệ thống để Kiali vẽ được Topology Graph.
3. Chụp lại màn hình sơ đồ kết nối hiển thị các đường kết nối màu xanh (mTLS thành công) để đưa vào file báo cáo `.docx`.

#### E. Viết Kịch Bản Test (Test Plan)
* Chui vào một pod nằm ngoài mesh (hoặc namespace không kích hoạt Istio sidecar) rồi dùng lệnh `curl` gửi request tới `cart.yas:80`. Xác nhận kết quả trả về bị chặn bởi Istio (ví dụ: lỗi `RBAC: access denied` hoặc lỗi kết nối).

---

## III. Các Bước Tiếp Theo Chúng Ta Nên Làm
Để giúp bạn hoàn thành xuất sắc đồ án này, chúng ta có thể thực hiện theo lộ trình sau:
1. **Bước 1:** Cập nhật tag image trong file CI (`cart-ci.yaml`) sang dạng **Commit SHA** thay vì tag `test` tĩnh.
2. **Bước 2:** Viết file cấu hình pipeline CD **developer-build.yaml** để đáp ứng yêu cầu số 4 (truyền tham số tên service và nhánh để deploy thử nghiệm).
3. **Bước 3:** Viết sẵn các file YAML manifest cấu hình Istio (`PeerAuthentication`, `VirtualService` chứa Retry Policy, `AuthorizationPolicy`) để bạn chỉ cần áp dụng (`kubectl apply -f`) lên máy ảo Azure.
