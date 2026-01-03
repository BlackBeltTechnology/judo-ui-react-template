import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import type { AxiosInstance, AxiosResponse } from 'axios';
import { JudoRuntimeService, X_JUDO_SIGNED_IDENTIFIER, X_JUDO_COUNT_RECORDS } from './runtime-service';
import type { AxiosProvider } from './runtime-service';
import type { JudoStored, QueryCustomizer } from './types';

// Mock types for testing
interface Galaxy {
  name: string;
  constellation?: string;
  magnitude?: number;
}

interface GalaxyStored extends Galaxy, JudoStored<Galaxy> {}

// Helper to create mock axios response
function createMockResponse<T>(data: T, status = 200): AxiosResponse<T> {
  return {
    data,
    status,
    statusText: 'OK',
    headers: {},
    config: { headers: {} } as any,
  };
}

// Helper to create a stored entity
function createStoredEntity<T>(data: T, signedId: string): T & JudoStored<T> {
  return {
    ...data,
    __signedIdentifier: signedId,
    __identifier: `id-${signedId}`,
    __entityType: 'TestEntity',
    __updateable: true,
    __deleteable: true,
  };
}

describe('JudoRuntimeService', () => {
  let mockAxios: {
    get: ReturnType<typeof vi.fn>;
    post: ReturnType<typeof vi.fn>;
    put: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let mockAxiosProvider: AxiosProvider;
  let runtimeService: JudoRuntimeService;

  const testConfig = {
    applicationName: 'ActionGroupTest',
    actorName: 'God',
    actorPath: 'God/God/God/God',
  };

  beforeEach(() => {
    mockAxios = {
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
    };

    mockAxiosProvider = {
      getAxios: () => mockAxios as unknown as AxiosInstance,
      getBasePath: (suffix?: string) => `/api${suffix ? '/' + suffix : ''}`,
    };

    runtimeService = new JudoRuntimeService(mockAxiosProvider, testConfig);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('listActorRelation - Galaxies Listing (Access Table)', () => {
    it('should list galaxies from actor relation without owner', async () => {
      const mockGalaxies: GalaxyStored[] = [
        createStoredEntity({ name: 'Andromeda', constellation: 'Andromeda', magnitude: 3.4 }, 'galaxy-1'),
        createStoredEntity({ name: 'Milky Way', constellation: 'Sagittarius', magnitude: -5 }, 'galaxy-2'),
        createStoredEntity({ name: 'Triangulum', constellation: 'Triangulum', magnitude: 5.7 }, 'galaxy-3'),
      ];

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxies));

      const result = await runtimeService.listActorRelation<GalaxyStored>('galaxies');

      expect(mockAxios.post).toHaveBeenCalledTimes(1);
      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        {},
        undefined,
      );
      expect(result.data).toHaveLength(3);
      expect(result.data[0].name).toBe('Andromeda');
      expect(result.data[1].name).toBe('Milky Way');
      expect(result.data[2].name).toBe('Triangulum');
    });

    it('should list galaxies with query customizer', async () => {
      const mockGalaxies: GalaxyStored[] = [
        createStoredEntity({ name: 'Andromeda', constellation: 'Andromeda' }, 'galaxy-1'),
      ];

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxies));

      const queryCustomizer: QueryCustomizer<GalaxyStored> = {
        _mask: '{name,constellation}',
        _orderBy: [{ attribute: 'name', descending: false }],
        _seek: { limit: 10 },
      };

      const result = await runtimeService.listActorRelation<GalaxyStored>('galaxies', undefined, queryCustomizer);

      expect(mockAxios.post).toHaveBeenCalledTimes(1);
      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        expect.objectContaining({
          _mask: '{name,constellation}',
          _orderBy: [{ attribute: 'name', descending: false }],
          _seek: { limit: 10 },
        }),
        undefined,
      );
      expect(result.data).toHaveLength(1);
    });

    it('should list galaxies with pagination (seek)', async () => {
      const mockGalaxies: GalaxyStored[] = [
        createStoredEntity({ name: 'Galaxy 11' }, 'galaxy-11'),
        createStoredEntity({ name: 'Galaxy 12' }, 'galaxy-12'),
      ];

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxies));

      const lastItem = createStoredEntity({ name: 'Galaxy 10' }, 'galaxy-10');
      const queryCustomizer: QueryCustomizer<GalaxyStored> = {
        _seek: { limit: 10, lastItem },
      };

      const result = await runtimeService.listActorRelation<GalaxyStored>('galaxies', undefined, queryCustomizer);

      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        expect.objectContaining({
          _seek: expect.objectContaining({ limit: 10 }),
        }),
        undefined,
      );
      expect(result.data).toHaveLength(2);
    });

    it('should list galaxies with count records header', async () => {
      const mockGalaxies: GalaxyStored[] = [
        createStoredEntity({ name: 'Andromeda' }, 'galaxy-1'),
      ];

      const responseWithCount = {
        ...createMockResponse(mockGalaxies),
        headers: { 'x-judo-count': '100' },
      };
      mockAxios.post.mockResolvedValueOnce(responseWithCount);

      await runtimeService.listActorRelation<GalaxyStored>(
        'galaxies',
        undefined,
        undefined,
        { [X_JUDO_COUNT_RECORDS]: 'true' },
      );

      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        {},
        { headers: { [X_JUDO_COUNT_RECORDS]: 'true' } },
      );
    });

    it('should list galaxies with owner (signed identifier)', async () => {
      const mockGalaxies: GalaxyStored[] = [
        createStoredEntity({ name: 'Sub Galaxy' }, 'sub-galaxy-1'),
      ];

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxies));

      const owner = createStoredEntity({}, 'owner-signed-id');

      const result = await runtimeService.listActorRelation<GalaxyStored>('galaxies', owner);

      expect(result.data).toHaveLength(1);
      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        {},
        {
          headers: {
            [X_JUDO_SIGNED_IDENTIFIER]: 'owner-signed-id',
          },
        },
      );
    });

    it('should return empty array when no galaxies exist', async () => {
      mockAxios.post.mockResolvedValueOnce(createMockResponse([]));

      const result = await runtimeService.listActorRelation<GalaxyStored>('galaxies');

      expect(result.data).toEqual([]);
      expect(result.data).toHaveLength(0);
    });

    it('should handle filtering in query customizer', async () => {
      const mockGalaxies: GalaxyStored[] = [
        createStoredEntity({ name: 'Andromeda', magnitude: 3.4 }, 'galaxy-1'),
      ];

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxies));

      const queryCustomizer: QueryCustomizer<GalaxyStored> = {
        _mask: '{name,magnitude}',
        name: [{ operator: 'like', value: 'Andro%' }],
      };

      await runtimeService.listActorRelation<GalaxyStored>('galaxies', undefined, queryCustomizer);

      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        expect.objectContaining({
          name: [{ operator: 'like', value: 'Andro%' }],
        }),
        undefined,
      );
    });
  });

  describe('refresh - Navigated Single Instance Request', () => {
    it('should refresh a single galaxy instance', async () => {
      const mockGalaxy: GalaxyStored = createStoredEntity(
        { name: 'Andromeda', constellation: 'Andromeda', magnitude: 3.4 },
        'galaxy-signed-id-123',
      );

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxy));

      const target = createStoredEntity({ name: 'Andromeda' }, 'galaxy-signed-id-123');

      const result = await runtimeService.refresh<GalaxyStored>('View/Galaxy', target);

      expect(mockAxios.post).toHaveBeenCalledTimes(1);
      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/View/Galaxy/~get',
        {},
        {
          headers: {
            [X_JUDO_SIGNED_IDENTIFIER]: 'galaxy-signed-id-123',
          },
        },
      );
      expect(result.data.name).toBe('Andromeda');
      expect(result.data.constellation).toBe('Andromeda');
      expect(result.data.magnitude).toBe(3.4);
    });

    it('should refresh with query customizer (mask)', async () => {
      const mockGalaxy: GalaxyStored = createStoredEntity(
        { name: 'Andromeda' },
        'galaxy-123',
      );

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxy));

      const target = createStoredEntity({}, 'galaxy-123');
      const queryCustomizer: QueryCustomizer<GalaxyStored> = {
        _mask: '{name}',
      };

      const result = await runtimeService.refresh<GalaxyStored>('View/Galaxy', target, queryCustomizer);

      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/View/Galaxy/~get',
        { _mask: '{name}' },
        {
          headers: {
            [X_JUDO_SIGNED_IDENTIFIER]: 'galaxy-123',
          },
        },
      );
      expect(result.data.name).toBe('Andromeda');
    });

    it('should refresh with nested class path', async () => {
      const mockGalaxy: GalaxyStored = createStoredEntity(
        { name: 'Nested Galaxy' },
        'nested-galaxy-456',
      );

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxy));

      const target = createStoredEntity({}, 'nested-galaxy-456');

      await runtimeService.refresh<GalaxyStored>('View/Universe/Galaxy', target);

      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/View/Universe/Galaxy/~get',
        {},
        {
          headers: {
            [X_JUDO_SIGNED_IDENTIFIER]: 'nested-galaxy-456',
          },
        },
      );
    });

    it('should refresh with additional headers', async () => {
      const mockGalaxy: GalaxyStored = createStoredEntity(
        { name: 'Andromeda' },
        'galaxy-789',
      );

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxy));

      const target = createStoredEntity({}, 'galaxy-789');

      await runtimeService.refresh<GalaxyStored>(
        'View/Galaxy',
        target,
        undefined,
        { 'X-Custom-Header': 'custom-value' },
      );

      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/View/Galaxy/~get',
        {},
        {
          headers: {
            [X_JUDO_SIGNED_IDENTIFIER]: 'galaxy-789',
            'X-Custom-Header': 'custom-value',
          },
        },
      );
    });

    it('should preserve stored metadata after refresh', async () => {
      const mockGalaxy: GalaxyStored = {
        ...createStoredEntity(
          { name: 'Updated Andromeda', constellation: 'Andromeda', magnitude: 3.5 },
          'galaxy-123',
        ),
        __updateable: true,
        __deleteable: false,
      };

      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxy));

      const target = createStoredEntity({ name: 'Andromeda' }, 'galaxy-123');

      const result = await runtimeService.refresh<GalaxyStored>('View/Galaxy', target);

      expect(result.data.__signedIdentifier).toBe('galaxy-123');
      expect(result.data.__updateable).toBe(true);
      expect(result.data.__deleteable).toBe(false);
      expect(result.data.name).toBe('Updated Andromeda');
    });
  });

  describe('Path Construction', () => {
    it('should construct correct path for actor relation operations', async () => {
      mockAxios.post.mockResolvedValueOnce(createMockResponse([]));

      await runtimeService.listActorRelation('galaxies');

      // Actor relation uses full repeated path: /api/{app}/{actorPath}/{relation}/~list
      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/God/God/galaxies/~list',
        {},
        undefined,
      );
    });

    it('should construct correct path for class-level operations', async () => {
      const mockGalaxy = createStoredEntity({ name: 'Test' }, 'test-123');
      mockAxios.post.mockResolvedValueOnce(createMockResponse(mockGalaxy));

      const target = createStoredEntity({}, 'test-123');
      await runtimeService.refresh('View/Galaxy', target);

      // Class-level uses base path: /api/{app}/{actorBase}/{classPath}/~get
      // actorBase = first half of actorPath = God/God
      expect(mockAxios.post).toHaveBeenCalledWith(
        '/api/ActionGroupTest/God/God/View/Galaxy/~get',
        {},
        {
          headers: {
            [X_JUDO_SIGNED_IDENTIFIER]: 'test-123',
          },
        },
      );
    });
  });

  describe('Error Handling', () => {
    it('should propagate axios errors for listActorRelation', async () => {
      const axiosError = new Error('Network Error');
      (axiosError as any).response = { status: 401, data: { message: 'Unauthorized' } };
      mockAxios.post.mockRejectedValueOnce(axiosError);

      await expect(runtimeService.listActorRelation('galaxies')).rejects.toThrow('Network Error');
    });

    it('should propagate axios errors for refresh', async () => {
      const axiosError = new Error('Not Found');
      (axiosError as any).response = { status: 404, data: { message: 'Entity not found' } };
      mockAxios.post.mockRejectedValueOnce(axiosError);

      const target = createStoredEntity({}, 'non-existent');

      await expect(runtimeService.refresh('View/Galaxy', target)).rejects.toThrow('Not Found');
    });
  });
});

