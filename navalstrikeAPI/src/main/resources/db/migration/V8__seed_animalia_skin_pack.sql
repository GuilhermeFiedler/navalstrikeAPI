INSERT INTO skin_packs (id, slug, name, description, created_at)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'animalia',
    'Animalia',
    'Navios inspirados na fauna marinha.',
    now()
) ON CONFLICT (slug) DO NOTHING;

INSERT INTO skin_assets (id, skin_pack_id, ship_type, image_url) VALUES
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'CARRIER', 'animalcarrier.png'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'BATTLESHIP', 'animalbattleship.png'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'CRUISER', 'animalcruiser.png'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'SUBMARINE', 'animalsubmarine.png'),
    (gen_random_uuid(), 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'DESTROYER', 'animaldestroyer.png')
ON CONFLICT (skin_pack_id, ship_type) DO NOTHING;
