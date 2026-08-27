CREATE TABLE appliance_category (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appliance (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES appliance_category(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    default_wattage NUMERIC(10, 2) NOT NULL,
    min_wattage NUMERIC(10, 2) NOT NULL,
    max_wattage NUMERIC(10, 2) NOT NULL,
    default_voltage INTEGER NOT NULL,
    surge_applicable BOOLEAN NOT NULL DEFAULT FALSE,
    surge_multiplier NUMERIC(4, 2) NOT NULL DEFAULT 1.0,
    heavy_load BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE calculation_configuration (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    version VARCHAR(30) NOT NULL UNIQUE,
    inverter_safety_margin NUMERIC(5, 4) NOT NULL,
    power_factor NUMERIC(4, 3) NOT NULL,
    battery_dod NUMERIC(4, 3) NOT NULL,
    battery_efficiency NUMERIC(4, 3) NOT NULL,
    inverter_efficiency NUMERIC(4, 3) NOT NULL,
    solar_efficiency NUMERIC(4, 3) NOT NULL,
    peak_sun_hours NUMERIC(4, 2) NOT NULL,
    default_panel_wattage NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inverter_option (
    id UUID PRIMARY KEY,
    rating_kva NUMERIC(6, 2) NOT NULL UNIQUE,
    continuous_watts NUMERIC(10, 2) NOT NULL,
    system_voltage INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE calculation (
    id UUID PRIMARY KEY,
    config_id UUID NOT NULL REFERENCES calculation_configuration(id),
    usage_mode VARCHAR(30) NOT NULL,
    backup_hours NUMERIC(4, 2),
    total_running_watts NUMERIC(10, 2) NOT NULL,
    peak_surge_watts NUMERIC(10, 2) NOT NULL,
    daily_energy_wh NUMERIC(12, 2) NOT NULL,
    recommended_inverter_kva NUMERIC(6, 2) NOT NULL,
    recommended_battery_ah NUMERIC(10, 2) NOT NULL,
    recommended_battery_kwh NUMERIC(10, 2) NOT NULL,
    recommended_solar_kw NUMERIC(8, 3) NOT NULL,
    panel_count INTEGER NOT NULL,
    input_payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
