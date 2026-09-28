const api = "/api";

const $ = id => document.getElementById(id);

// Store products globally so new order rows can use them
let availableProducts = [];

/* =========================
   NAVIGATION
========================= */

document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".nav-btn")
            .forEach(b => b.classList.remove("active"));

        document.querySelectorAll(".section")
            .forEach(s => s.classList.remove("active"));

        btn.classList.add("active");

        const section = $(btn.dataset.section);

        if (section) {
            section.classList.add("active");
        }

        loadAll();
    });
});


/* =========================
   API REQUEST
========================= */

async function request(url, options = {}) {

    const response = await fetch(url, options);

    const text = await response.text();

    let data = {};

    try {
        data = text ? JSON.parse(text) : {};
    } catch {
        data = text;
    }

    if (!response.ok) {
        throw new Error(
            data.message || `Request failed (${response.status})`
        );
    }

    return data;
}


/* =========================
   TOAST MESSAGE
========================= */

function toast(message) {

    const toastElement = $("toast");

    if (!toastElement) return;

    toastElement.textContent = message;
    toastElement.style.display = "block";

    setTimeout(() => {
        toastElement.style.display = "none";
    }, 2200);
}


/* =========================
   LOAD ALL DATA
========================= */

async function loadAll() {

    try {

        const [
            summary,
            products,
            bins,
            inventory,
            orders,
            productReport,
            binReport
        ] = await Promise.all([

            request("/api/reports/summary"),

            request("/api/products"),

            request("/api/bins"),

            request("/api/inventory"),

            request("/api/orders"),

            request("/api/reports/products"),

            request("/api/reports/bins")
        ]);


        // Save products globally
        availableProducts = products || [];


        // Render all sections
        renderSummary(summary);

        renderProducts(products);

        renderBins(bins);

        renderInventory(inventory);

        renderOrders(orders);

        renderProductReport(productReport);

        renderBinReport(binReport);


        // Fill product dropdowns
        fillProductSelects(availableProducts);


        // Create first order item row only once
        if (!$("orderLines").children.length) {

            addOrderLine();

        }

    } catch (e) {

        console.error(e);

        toast(e.message);
    }
}


/* =========================
   DASHBOARD SUMMARY
========================= */

function renderSummary(s) {

    $("cards").innerHTML = [

        ["Products", s.products],

        ["Bins", s.bins],

        ["Occupied", s.occupiedCapacity],

        [
            "Utilization",
            `${Number(s.utilizationPercent).toFixed(1)}%`
        ]

    ]
        .map(x => `
            <div class="card">
                <div class="muted">${x[0]}</div>
                <div class="value">${x[1]}</div>
            </div>
        `)
        .join("");
}


/* =========================
   PRODUCTS TABLE
========================= */

function renderProducts(items) {

    $("productsTable").innerHTML = table(

        [
            "ID",
            "SKU",
            "Name",
            "Velocity",
            "Active"
        ],

        items.map(p => [

            p.id,

            p.sku,

            p.name,

            `
            <span class="badge ${(p.velocity || "MEDIUM").toLowerCase()}">
                ${p.velocity || "MEDIUM"}
            </span>
            `,

            p.active ? "Yes" : "No"

        ])
    );
}


/* =========================
   BINS TABLE
========================= */

function renderBins(items) {

    $("binsTable").innerHTML = table(

        [
            "Code",
            "Zone",
            "Capacity",
            "Occupied",
            "Available",
            "Route"
        ],

        items.map(b => [

            b.binCode,

            b.zone,

            b.capacity,

            b.currentOccupancy,

            b.capacity - b.currentOccupancy,

            b.routeOrder

        ])
    );
}


/* =========================
   INVENTORY TABLE
========================= */

function renderInventory(items) {

    $("inventoryTable").innerHTML = table(

        [
            "Product",
            "SKU",
            "Quantity",
            "Bin",
            "Zone"
        ],

        items.map(i => [

            i.productName,

            i.sku,

            i.quantity,

            i.binCode,

            i.zone

        ])
    );
}


/* =========================
   ORDERS TABLE
========================= */

function renderOrders(items) {

    $("ordersTable").innerHTML = table(

        [
            "ID",
            "Customer",
            "Status",
            "Created",
            "Action"
        ],

        items.map(o => [

            o.id,

            o.customerName,

            o.status,

            new Date(o.createdAt).toLocaleString(),

            `
            <button onclick="showPicklist(${o.id})">
                Picklist
            </button>

            ${
                o.status === "PENDING"

                    ? `
                    <button onclick="pickOrder(${o.id})">
                        Pick Order
                    </button>
                    `

                    : ""
            }
            `

        ])
    );
}


/* =========================
   PRODUCT REPORT
========================= */

function renderProductReport(items) {

    $("productReport").innerHTML = table(

        [
            "SKU",
            "Product",
            "Velocity",
            "Stock"
        ],

        items.map(p => [

            p.sku,

            p.name,

            p.velocity,

            p.stock

        ])
    );
}


/* =========================
   BIN REPORT
========================= */

function renderBinReport(items) {

    $("binReport").innerHTML = table(

        [
            "Bin",
            "Zone",
            "Capacity",
            "Occupied",
            "Available",
            "Utilization"
        ],

        items.map(b => [

            b.binCode,

            b.zone,

            b.capacity,

            b.occupied,

            b.available,

            `${Number(b.utilizationPercent).toFixed(1)}%`

        ])
    );
}


/* =========================
   COMMON TABLE
========================= */

function table(headers, rows) {

    if (!rows.length) {

        return `
            <p class="muted">
                No records yet.
            </p>
        `;
    }

    return `
        <div style="overflow:auto">

            <table>

                <thead>

                    <tr>

                        ${headers
                            .map(h => `<th>${h}</th>`)
                            .join("")}

                    </tr>

                </thead>

                <tbody>

                    ${
                        rows
                            .map(row => `
                                <tr>

                                    ${
                                        row
                                            .map(cell => `
                                                <td>${cell}</td>
                                            `)
                                            .join("")
                                    }

                                </tr>
                            `)
                            .join("")
                    }

                </tbody>

            </table>

        </div>
    `;
}


/* =========================
   PRODUCT DROPDOWNS
========================= */

function fillProductSelects(products) {

    const activeProducts = products.filter(
        p => p.active
    );


    /* Receive Stock dropdown */

    const receiveProduct = $("receiveProduct");

    if (receiveProduct) {

        receiveProduct.innerHTML = activeProducts

            .map(p => `
                <option value="${p.id}">
                    ${p.name} (${p.sku})
                </option>
            `)

            .join("");
    }


    /* Order product dropdowns */

    document
        .querySelectorAll(".order-product")
        .forEach(select => {

            const currentValue = select.value;

            select.innerHTML = activeProducts

                .map(p => `
                    <option value="${p.id}">
                        ${p.name} (${p.sku})
                    </option>
                `)

                .join("");


            // Preserve selected product if possible
            if (currentValue) {

                const optionExists =
                    [...select.options]
                        .some(option =>
                            option.value === currentValue
                        );

                if (optionExists) {

                    select.value = currentValue;

                }

            }

        });
}


/* =========================
   PRODUCT FORM
========================= */

$("productForm").addEventListener(
    "submit",
    async e => {

        e.preventDefault();

        try {

            await request("/api/products", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({

                    sku: $("sku").value,

                    name: $("productName").value,

                    description: $("description").value,

                    velocity: $("velocity").value

                })

            });


            e.target.reset();

            $("velocity").value = "MEDIUM";

            toast("Product added");

            await loadAll();

        } catch (e) {

            toast(e.message);
        }

    }
);


/* =========================
   BIN FORM
========================= */

$("binForm").addEventListener(
    "submit",
    async e => {

        e.preventDefault();

        try {

            await request("/api/bins", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({

                    binCode: $("binCode").value,

                    zone: $("zone").value,

                    capacity: Number(
                        $("capacity").value
                    ),

                    routeOrder: Number(
                        $("routeOrder").value
                    )

                })

            });


            e.target.reset();

            toast("Bin added");

            await loadAll();

        } catch (e) {

            toast(e.message);
        }

    }
);


/* =========================
   RECEIVE STOCK FORM
========================= */

$("receiveForm").addEventListener(
    "submit",
    async e => {

        e.preventDefault();

        try {

            const data = await request(
                "/api/inventory/receive",
                {

                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({

                        productId: Number(
                            $("receiveProduct").value
                        ),

                        quantity: Number(
                            $("receiveQuantity").value
                        )

                    })

                }
            );


            $("receiveResult").innerHTML = `

                <div class="success">

                    <b>
                        Stock received successfully.
                    </b>

                    <br>

                    ${data.productName}:
                    ${data.quantityReceived}
                    units →

                    <b>
                        ${data.assignedBin}
                    </b>

                    (${data.zone}),

                    occupancy
                    ${data.previousOccupancy}
                    →
                    ${data.newOccupancy}.

                </div>

            `;


            e.target.reset();

            toast("Stock slotted automatically");

            await loadAll();

        } catch (e) {

            $("receiveResult").innerHTML = `

                <div class="error">
                    ${e.message}
                </div>

            `;

        }

    }
);


/* =========================
   ADD ORDER ITEM
========================= */

function addOrderLine() {

    const row = document.createElement("div");

    row.className = "order-line";


    row.innerHTML = `

        <select class="order-product">
        </select>

        <input
            class="order-qty"
            type="number"
            min="1"
            value="1"
        >

        <button
            type="button"
            onclick="this.parentElement.remove()"
        >
            Remove
        </button>

    `;


    $("orderLines").appendChild(row);


    /*
       IMPORTANT:
       Do NOT call loadAll() here.

       Products are already stored in
       availableProducts.
    */

    const select =
        row.querySelector(".order-product");


    const activeProducts =
        availableProducts.filter(
            p => p.active
        );


    select.innerHTML = activeProducts

        .map(p => `
            <option value="${p.id}">
                ${p.name} (${p.sku})
            </option>
        `)

        .join("");
}


/* =========================
   CREATE ORDER
========================= */

$("orderForm").addEventListener(
    "submit",
    async e => {

        e.preventDefault();


        const rows =
            [
                ...document.querySelectorAll(
                    ".order-line"
                )
            ];


        if (!rows.length) {

            toast("Add at least one item");

            return;
        }


        const items = rows.map(row => ({

            productId: Number(
                row.querySelector(
                    ".order-product"
                ).value
            ),

            quantity: Number(
                row.querySelector(
                    ".order-qty"
                ).value
            )

        }));


        try {

            const order = await request(
                "/api/orders",
                {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        customerName:
                            $("customerName").value,

                        items: items

                    })

                }
            );


            toast(
                `Order #${order.id} created`
            );


            $("customerName").value = "";


            $("orderLines").innerHTML = "";


            // Create fresh first item
            addOrderLine();


            await loadAll();

        } catch (e) {

            toast(e.message);
        }

    }
);


/* =========================
   SHOW PICKLIST
========================= */

async function showPicklist(id) {

    try {

        const lines =
            await request(
                `/api/orders/${id}/picklist`
            );


        $("picklist").innerHTML = table(

            [
                "Route",
                "Bin",
                "Zone",
                "Product",
                "Quantity"
            ],

            lines.map(x => [

                x.routeOrder,

                x.binCode,

                x.zone,

                x.productName,

                x.quantity

            ])

        );

    } catch (e) {

        toast(e.message);
    }
}


/* =========================
   PICK ORDER
========================= */

async function pickOrder(id) {

    if (
        !confirm(
            `Complete picking for order #${id}?`
        )
    ) {

        return;
    }


    try {

        await request(
            `/api/orders/${id}/pick`,
            {
                method: "POST"
            }
        );


        toast(
            "Order picked and bin occupancy updated"
        );


        await loadAll();


        await showPicklist(id);

    } catch (e) {

        toast(e.message);
    }
}


/* =========================
   HEALTH CHECK
========================= */

async function checkHealth() {

    try {

        await request("/api/health");

        $("health").textContent =
            "Backend Online";

    } catch {

        $("health").textContent =
            "Backend Offline";
    }
}


/* =========================
   INITIAL LOAD
========================= */

checkHealth();

loadAll();