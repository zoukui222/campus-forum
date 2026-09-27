package com.zwz.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwz.forum.dto.BoardRequest;
import com.zwz.forum.entity.Board;
import com.zwz.forum.mapper.BoardMapper;
import com.zwz.forum.service.IBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 板块表 服务实现类
 * </p>
 *
 * @author ljx
 * @since 2025-11-21
 */
@Service
public class BoardServiceImpl extends ServiceImpl<BoardMapper, Board> implements IBoardService {
    @Transactional
    public void createBoard(BoardRequest req) {
        Board board = new Board();
        board.setName(req.name());
        board.setDescription(req.description());
        board.setSort(req.sort());
        board.setIcon(req.icon());
        this.save(board);
    }

    @Transactional
    public void deleteBoard(Integer id) {
        // 如果需要严谨点可以先检查该板块下有没有帖子，如果有则禁止删除
        this.removeById(id);
    }
}
