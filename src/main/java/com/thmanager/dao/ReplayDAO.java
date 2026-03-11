package com.thmanager.dao;

import com.thmanager.mapper.ReplayMapper;
import com.thmanager.model.Replay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Replay数据访问对象（DAO）
 * 
 * 提供Replay实体的数据库操作方法，包括：
 * - 保存和更新Replay
 * - 查询Replay（按各种条件）
 * - 删除Replay
 * - 分页查询Replay
 * 
 * @Repository 注解表示这是一个数据访问层组件。
 */
@Repository
public class ReplayDAO {

    /**
     * Replay MyBatis Mapper
     */
    private final ReplayMapper replayMapper;

    /**
     * 构造函数，依赖注入ReplayMapper
     * 
     * @param replayMapper Replay数据库映射器
     */
    @Autowired
    public ReplayDAO(ReplayMapper replayMapper) {
        this.replayMapper = replayMapper;
    }

    /**
     * 保存或更新Replay
     * 
     * 如果Replay文件路径已存在，则更新现有记录；否则创建新记录
     * 
     * @param replay 要保存或更新的Replay对象
     * @return 操作是否成功
     */
    public boolean saveOrUpdate(Replay replay) {
        Optional<Replay> existing = findByPath(replay.getFilePath());
        if (existing.isPresent()) {
            System.out.println("Replay已存在，更新: " + replay.getFilePath());
            replay.setId(existing.get().getId());
            return update(replay);
        }
        return create(replay);
    }

    /**
     * 更新现有Replay记录
     * 
     * @param replay 要更新的Replay对象（必须包含id）
     * @return 更新是否成功
     */
    public boolean update(Replay replay) {
        int result = replayMapper.updateReplay(replay);
        if (result > 0) {
            System.out.println("✓ Replay已更新: " + replay.getFileName());
            return true;
        }
        return false;
    }

    /**
     * 创建新的Replay记录
     * 
     * @param replay 要创建的Replay对象
     * @return 创建是否成功
     */
    public boolean create(Replay replay) {
        try {
            int result = replayMapper.insertReplay(replay);
            if (result > 0) {
                System.out.println("✓ Replay已保存: " + replay.getFileName());
                return true;
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            }
            System.err.println("保存Replay失败: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据文件路径查找Replay
     * 
     * @param filePath Replay文件完整路径
     * @return 包含Replay的Optional对象，如果不存在则为空
     */
    public Optional<Replay> findByPath(String filePath) {
        return replayMapper.findByPathWithGameTitle(filePath);
    }

    /**
     * 根据游戏ID查找该游戏的所有Replay
     * 
     * @param gameId 游戏ID
     * @return 该游戏的Replay列表，按导入时间降序排列
     */
    public List<Replay> findByGame(int gameId) {
        return replayMapper.findByGameIdWithGameTitle(gameId);
    }

    /**
     * 查找指定游戏和难度的最高分Replay
     * 
     * @param gameId     游戏ID
     * @param difficulty 难度
     * @return 包含最高分Replay的Optional对象
     */
    public Optional<Replay> findBestByDifficulty(int gameId, String difficulty) {
        return replayMapper.findBestByDifficultyWithGameTitle(gameId, difficulty);
    }

    /**
     * 查找最近导入的Replay
     * 
     * @param limit 返回的最大数量
     * @return 最近导入的Replay列表
     */
    public List<Replay> findRecent(int limit) {
        return replayMapper.findRecentWithGameTitle(limit);
    }

    /**
     * 获取所有Replay
     * 
     * @return 所有Replay列表，按导入时间降序排列
     */
    public List<Replay> findAll() {
        return replayMapper.findAllWithGameTitle();
    }

    /**
     * 分页查询所有Replay
     * 
     * @param page     页码（从1开始）
     * @param pageSize 每页数量
     * @return 分页的Replay列表
     */
    public List<Replay> findAllPaged(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return replayMapper.findAllWithGameTitlePaged(offset, pageSize);
    }

    /**
     * 获取Replay总数
     * 
     * @return Replay记录总数
     */
    public int countAll() {
        return replayMapper.countAll();
    }

    /**
     * 根据ID查找Replay
     * 
     * @param id Replay ID
     * @return 包含Replay的Optional对象
     */
    public Optional<Replay> findById(int id) {
        return replayMapper.findByIdWithGameTitle(id);
    }

    /**
     * 删除指定ID的Replay
     * 
     * @param id Replay ID
     * @return 删除是否成功
     */
    public boolean delete(int id) {
        return replayMapper.deleteReplayById(id) > 0;
    }
}
